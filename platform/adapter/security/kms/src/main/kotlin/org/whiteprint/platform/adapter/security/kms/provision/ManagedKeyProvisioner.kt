package org.whiteprint.platform.adapter.security.kms.provision

import org.slf4j.LoggerFactory
import org.whiteprint.platform.core.kms.service.KeyAdminOperations

/**
 * 선언된 키를 KMS 에 맞춘다: 없으면 만들고, 있는데 종류가 다르면 기동을 멈춘다.
 *
 * 키를 지우거나 바꾸지는 않는다 — 종류가 다른 키를 덮으면 그 키로 만든 서명·암호문이 전부 죽는다.
 * 여러 인스턴스가 동시에 기동해도 된다: 같은 이름으로 다시 만들면 Vault 는 기존 키를 그대로 둔다.
 */
class ManagedKeyProvisioner(
    private val adminOperations: KeyAdminOperations,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun provision(keys: List<ManagedKey>) {
        resolveDeclarations(keys).forEach { (alias, declared) ->
            val existing = adminOperations.findMetadata(alias)
            if (existing == null) {
                adminOperations.createKey(alias, declared)
                logger.info("KMS key created: alias={}, type={}", alias, declared)
            }

            val actual = (existing ?: adminOperations.findMetadata(alias))?.type
            check(actual == declared) {
                "KMS key '$alias' is $actual but declared as $declared — fix the declaration or the key; it is never replaced automatically"
            }
        }
    }

    /** 이름 중복은 종류까지 같을 때만 허용한다(설정과 모듈이 같은 키를 둘 다 적는 경우). */
    private fun resolveDeclarations(keys: List<ManagedKey>) =
        keys.groupBy { it.alias }.map { (alias, declarations) ->
            require(alias.isNotBlank()) { "KMS managed key alias must not be blank" }
            val types = declarations.map { it.type }.toSet()
            require(types.size == 1) { "KMS managed key '$alias' is declared with different types: $types" }
            val type = requireNotNull(types.single()) { "KMS managed key '$alias' has no type" }
            alias to type
        }
}
