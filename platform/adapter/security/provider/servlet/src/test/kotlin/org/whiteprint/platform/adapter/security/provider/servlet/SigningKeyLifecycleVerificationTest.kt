package org.whiteprint.platform.adapter.security.provider.servlet

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.adapter.security.kms.provision.ManagedKey
import org.whiteprint.platform.adapter.security.kms.provision.ManagedKeyProvisioner
import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKeyPolicyConfigurationProperties.AccessTokenKeyPolicy
import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderKeyPolicyConfigurationProperties.RefreshTokenKeyPolicy
import org.whiteprint.platform.adapter.security.provider.servlet.key.AccessTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenSignerImpl
import org.whiteprint.platform.adapter.security.provider.servlet.key.RefreshTokenVerificationKeyResolverImpl
import org.whiteprint.platform.adapter.security.provider.servlet.rotation.SigningKeyRotator
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyType
import org.whiteprint.platform.infra.kms.vault.CaffeineKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyAdminOperations
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialProvider
import org.whiteprint.platform.infra.kms.vault.VaultKeyOperations
import java.security.PublicKey
import java.security.Signature
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

/**
 * 서명 키 생성·회전·버전 고정 서명을 **실제 Vault transit** 으로 실측한다.
 *
 * `whiteprint-test-<실행 UUID>-*` 키만 만들고 끝나면 지운다. 접속 실패 시 skip.
 * 접속 override: KMS_VAULT_HOST / KMS_VAULT_PORT / KMS_VAULT_TOKEN (기본: 127.0.0.1:18200, wp-kms-test)
 * 일회용 Vault: `docker run --rm -p 127.0.0.1:18200:8200 -e VAULT_DEV_ROOT_TOKEN_ID=wp-kms-test
 *   -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 hashicorp/vault:1.21.4` 후 `vault secrets enable transit`
 */
@EnabledIf("vaultAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SigningKeyLifecycleVerificationTest {

    companion object {
        private val host = System.getenv("KMS_VAULT_HOST") ?: "127.0.0.1"
        private val port = (System.getenv("KMS_VAULT_PORT") ?: "18200").toInt()
        private val token = System.getenv("KMS_VAULT_TOKEN") ?: "wp-kms-test"
        private const val TRANSIT = "transit"

        private val vault: VaultTemplate by lazy {
            VaultTemplate(
                VaultEndpoint.create(host, port).apply { scheme = "http" },
                TokenAuthentication(token),
            )
        }

        @JvmStatic
        fun vaultAvailable(): Boolean =
            runCatching { vault.opsForTransit(TRANSIT).getKeys(); true }.getOrDefault(false)
    }

    private val run = UUID.randomUUID().toString().take(8)
    private val created = mutableListOf<String>()

    private val keyCache = CaffeineKeyCache(expiresAfterWriteMinutes = 60, maximumSize = 100)
    private val materialProvider = VaultKeyMaterialProvider(vault, keyCache, TRANSIT)
    private val adminOperations = VaultKeyAdminOperations(vault, materialProvider, TRANSIT)
    private val keyOperations = VaultKeyOperations(vault, TRANSIT)
    private val provisioner = ManagedKeyProvisioner(adminOperations)

    private fun alias(name: String) = "whiteprint-test-$run-$name".also { created += it }

    @AfterAll
    fun cleanup() {
        created.distinct().forEach { alias ->
            runCatching {
                vault.write("$TRANSIT/keys/$alias/config", mapOf("deletion_allowed" to true))
                vault.delete("$TRANSIT/keys/$alias")
            }
        }
    }

    @Test
    @DisplayName("없는 키는 선언한 종류로 만든다 — 개인키는 내보낼 수 없고, 삭제는 막혀 있다")
    fun createsMissingKeys() {
        val rsa = alias("rsa")
        val aes = alias("aes")

        provisioner.provision(listOf(ManagedKey(rsa, KeyType.RSA_2048), ManagedKey(aes, KeyType.AES_256_GCM)))

        val rsaMeta = adminOperations.findMetadata(rsa)!!
        assertEquals(KeyType.RSA_2048, rsaMeta.type)
        assertEquals("1", rsaMeta.latestVersion)
        assertFalse(rsaMeta.isExportable)
        assertFalse(rsaMeta.isDeletable)

        val aesMeta = adminOperations.findMetadata(aes)!!
        assertEquals(KeyType.AES_256_GCM, aesMeta.type)
        assertFalse(aesMeta.isDeletable)
    }

    @Test
    @DisplayName("대칭키도 createKey 가 실패하지 않는다 — 공개키가 없어 메타데이터만 돌려준다")
    fun createKeyReturnsMetadataForSymmetricKey() {
        val aes = alias("aes-direct")

        val bundle = adminOperations.createKey(aes, KeyType.AES_256_GCM)

        assertNull(bundle.material)
        assertEquals(KeyType.AES_256_GCM, bundle.metadata.type)
    }

    @Test
    @DisplayName("다시 돌려도 새로 만들거나 버전을 올리지 않는다")
    fun provisionIsIdempotent() {
        val rsa = alias("idempotent")
        val keys = listOf(ManagedKey(rsa, KeyType.RSA_2048))

        provisioner.provision(keys)
        provisioner.provision(keys)
        adminOperations.createKey(rsa, KeyType.RSA_2048) // 인스턴스 둘이 동시에 만드는 경우

        assertEquals("1", adminOperations.findMetadata(rsa)!!.latestVersion)
    }

    @Test
    @DisplayName("이미 있는 키의 종류가 선언과 다르면 기동을 멈추고, 키는 건드리지 않는다")
    fun typeMismatchFailsWithoutTouchingKey() {
        val rsa = alias("mismatch")
        provisioner.provision(listOf(ManagedKey(rsa, KeyType.RSA_2048)))

        val error = assertThrows(IllegalStateException::class.java) {
            provisioner.provision(listOf(ManagedKey(rsa, KeyType.AES_256_GCM)))
        }

        assertTrue(error.message!!.contains(rsa))
        val meta = adminOperations.findMetadata(rsa)!!
        assertEquals(KeyType.RSA_2048, meta.type)
        assertEquals("1", meta.latestVersion)
    }

    @Test
    @DisplayName("같은 이름을 다른 종류로 두 번 선언하면 Vault 를 부르기 전에 실패한다")
    fun conflictingDeclarationsFail() {
        val name = alias("conflict")

        assertThrows(IllegalArgumentException::class.java) {
            provisioner.provision(listOf(ManagedKey(name, KeyType.RSA_2048), ManagedKey(name, KeyType.RSA_4096)))
        }
        assertNull(adminOperations.findMetadata(name))
    }

    @Test
    @DisplayName("회전: 주기 전이면 그대로, 지나면 새 버전, 주기 0 이하는 회전 안 함")
    fun rotatesOnlyWhenDue() {
        val due = alias("due")
        val off = alias("off")
        provisioner.provision(listOf(ManagedKey(due, KeyType.RSA_2048), ManagedKey(off, KeyType.RSA_2048)))
        val policies = listOf(
            SigningKeyRotator.RotationPolicy(due, Duration.ofDays(30)),
            SigningKeyRotator.RotationPolicy(off, Duration.ZERO),
        )

        val now = SigningKeyRotator(adminOperations, policies)
        assertEquals(0, now.rotateDueKeys())
        assertEquals("1", adminOperations.findMetadata(due)!!.latestVersion)

        val later = SigningKeyRotator(adminOperations, policies, clockAt(Instant.now().plus(Duration.ofDays(31))))
        assertEquals(1, later.rotateDueKeys())
        assertEquals("2", adminOperations.findMetadata(due)!!.latestVersion)
        assertEquals("1", adminOperations.findMetadata(off)!!.latestVersion)

        // 방금 만든 v2 기준으로 다시 30일을 센다
        assertEquals(0, now.rotateDueKeys())
    }

    @Test
    @DisplayName("헤더용으로 고른 버전으로 서명된다 — 사이에 키가 회전해도")
    fun signingIsPinnedToChosenVersion() {
        val access = alias("access")
        provisioner.provision(listOf(ManagedKey(access, KeyType.RSA_2048)))
        val signer = AccessTokenSignerImpl(keyOperations, adminOperations, AccessTokenKeyPolicy(keyAlias = access))

        val chosen = signer.getLatestSigningKeyMetadata()
        adminOperations.rotateKey(access) // 헤더를 만든 직후 회전

        val signed = signer.sign(chosen, "header.payload")

        assertEquals("1", chosen.keyVersion)
        assertEquals("1", signed.keyVersion)
        assertTrue(keyOperations.verify(KeyId(access, "1"), "header.payload", signed.signature))
        // 고정하지 않으면 최신(v2)으로 서명된다 — 고치기 전의 동작
        assertEquals("2", keyOperations.sign(access, "header.payload").keyId.version)
    }

    @Test
    @DisplayName("회전 전에 발급한 refresh 토큰 서명은 회전 뒤에도 그 버전 공개키로 검증된다")
    fun tokensSignedBeforeRotationStillVerify() {
        val refresh = alias("refresh")
        provisioner.provision(listOf(ManagedKey(refresh, KeyType.RSA_2048)))
        val signer = RefreshTokenSignerImpl(keyOperations, adminOperations, RefreshTokenKeyPolicy(keyAlias = refresh))
        val resolver = RefreshTokenVerificationKeyResolverImpl(materialProvider)

        val before = signer.getLatestSigningKeyMetadata()
        val oldSignature = signer.sign(before, "old.token").signature

        adminOperations.rotateKey(refresh)
        val after = signer.getLatestSigningKeyMetadata()
        val newSignature = signer.sign(after, "new.token").signature

        assertEquals("2", after.keyVersion)
        // JwtRefreshTokenVerifier 와 같은 방식(SHA256withRSA, 헤더의 ver 로 공개키 조회)
        assertTrue(verifyRsa(resolver.resolve(refresh, "1").verifyKey, "old.token", oldSignature))
        assertTrue(verifyRsa(resolver.resolve(refresh, "2").verifyKey, "new.token", newSignature))
        assertFalse(verifyRsa(resolver.resolve(refresh, "2").verifyKey, "old.token", oldSignature))
    }

    private fun verifyRsa(key: PublicKey, text: String, signature: ByteArray): Boolean =
        Signature.getInstance("SHA256withRSA").run {
            initVerify(key)
            update(text.toByteArray(Charsets.UTF_8))
            verify(signature)
        }

    private fun clockAt(instant: Instant): Clock = Clock.fixed(instant, ZoneOffset.UTC)
}
