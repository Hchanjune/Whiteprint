package org.whiteprint.platform.adapter.security.kms.provision

import org.whiteprint.platform.core.kms.model.KeyType

/** 기동 시 KMS 에 있어야 하는 키 하나. 없으면 [type] 으로 만들고, 있으면 종류가 같은지만 본다. */
data class ManagedKey(
    var alias: String = "",
    var type: KeyType? = null,
)

/**
 * 모듈이 자기 키를 [ManagedKeyProvisioner] 에 알리는 통로 — 설정(`adapter.security.kms.keys`)에 적지 않아도 된다.
 * 예: provider 는 토큰 서명 키 2개를 여기로 올린다.
 */
fun interface ManagedKeyContributor {
    fun managedKeys(): List<ManagedKey>
}
