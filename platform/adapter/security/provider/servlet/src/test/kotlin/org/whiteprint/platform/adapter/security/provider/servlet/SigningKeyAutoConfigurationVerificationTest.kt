package org.whiteprint.platform.adapter.security.provider.servlet

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate
import org.whiteprint.platform.adapter.security.kms.configuration.KmsAutoConfiguration
import org.whiteprint.platform.adapter.security.provider.servlet.configuration.SecurityProviderAutoConfiguration
import org.whiteprint.platform.adapter.security.provider.servlet.rotation.SigningKeyRotator
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.kms.model.KeyType
import org.whiteprint.platform.core.kms.service.KeyAdminOperations
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.infra.kms.vault.CaffeineKeyCache
import org.whiteprint.platform.infra.kms.vault.VaultKeyAdminOperations
import org.whiteprint.platform.infra.kms.vault.VaultKeyMaterialProvider
import java.util.UUID

/**
 * 자동 설정 배선을 **실제 Vault** 로 실측한다 — 기동 중 키 생성, 잘못된 알고리즘의 기동 실패, 회전 스케줄 등록.
 * 접속·정리 규칙은 [SigningKeyLifecycleVerificationTest] 와 같다.
 */
@EnabledIf("vaultAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SigningKeyAutoConfigurationVerificationTest {

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

    private fun runner(vararg properties: String) =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KmsAutoConfiguration::class.java, SecurityProviderAutoConfiguration::class.java))
            .withBean(Serializer::class.java, { mock(Serializer::class.java) })
            .withBean(RevocationChecker::class.java, { mock(RevocationChecker::class.java) })
            .withPropertyValues(
                "adapter.security.kms.datasource.host=$host",
                "adapter.security.kms.datasource.port=$port",
                "adapter.security.kms.datasource.password=$token",
                *properties,
            )

    @Test
    @DisplayName("기동하면 서명 키 2개와 설정에 적은 키가 만들어지고, 회전 스케줄이 등록된다")
    fun createsKeysOnStartup() {
        val access = alias("access")
        val refresh = alias("refresh")
        val billing = alias("billing")

        runner(
            "adapter.security.provider.key-policy.access-token.key-alias=$access",
            "adapter.security.provider.key-policy.refresh-token.key-alias=$refresh",
            "adapter.security.kms.keys[0].alias=$billing",
            "adapter.security.kms.keys[0].type=AES_256_GCM",
        ).run { context ->
            assertNull(context.startupFailure)
            val admin = context.getBean(KeyAdminOperations::class.java)
            assertEquals(KeyType.RSA_2048, admin.findMetadata(access)!!.type)
            assertEquals(KeyType.RSA_2048, admin.findMetadata(refresh)!!.type)
            assertEquals(KeyType.AES_256_GCM, admin.findMetadata(billing)!!.type)

            assertNotNull(context.getBean(SigningKeyRotator::class.java))
            assertTrue(context.getBeansOfType(ScheduledAnnotationBeanPostProcessor::class.java).isNotEmpty())
        }
    }

    @Test
    @DisplayName("서명 알고리즘이 RSA 가 아니면(예: 예전 설정의 HMAC_SHA256) 기동이 멈추고 키는 만들지 않는다")
    fun rejectsNonRsaSigningAlgorithm() {
        val access = alias("access-hmac")
        val refresh = alias("refresh-hmac")

        runner(
            "adapter.security.provider.key-policy.access-token.key-alias=$access",
            "adapter.security.provider.key-policy.refresh-token.key-alias=$refresh",
            "adapter.security.provider.key-policy.refresh-token.algorithm=HMAC_SHA256",
        ).run { context ->
            val failure = context.startupFailure
            assertNotNull(failure)
            val messages = generateSequence(failure) { it.cause }.mapNotNull { it.message }.joinToString(" | ")
            assertTrue(messages.contains("refresh-token.algorithm must be one of"), messages)

            // 실패한 컨텍스트에서는 빈을 못 꺼내므로 조회용 연결을 따로 만든다
            val admin = VaultKeyAdminOperations(vault, VaultKeyMaterialProvider(vault, CaffeineKeyCache(60, 10), TRANSIT), TRANSIT)
            assertNull(admin.findMetadata(refresh))
        }
    }
}
