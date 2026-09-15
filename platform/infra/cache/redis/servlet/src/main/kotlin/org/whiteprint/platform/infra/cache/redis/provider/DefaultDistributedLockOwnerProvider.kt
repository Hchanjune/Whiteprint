package org.whiteprint.platform.infra.cache.redis.provider

import org.whiteprint.platform.core.lock.provider.DistributedLockOwnerProvider
import java.net.InetAddress
import java.util.UUID

/**
 * 인스턴스 식별자(`호스트명:UUID8`). 락 owner 의 **앞부분**일 뿐이다 — 실제 owner 는 획득마다
 * `RedisDistributedLockOperations` 가 UUID 를 덧붙여 만든다. 이 값을 그대로 owner 로 쓰면 같은 인스턴스의
 * 다른 스레드가 잡은 락을 풀 수 있다.
 */
class DefaultDistributedLockOwnerProvider : DistributedLockOwnerProvider {

    companion object {
        private val OWNER_ID = try {
            "${InetAddress.getLocalHost().hostName}:${UUID.randomUUID().toString().take(8)}"
        } catch (exception: Exception) {
            UUID.randomUUID().toString()
        }

        fun currentOwner(): String = OWNER_ID
    }

    override fun provideOwner(): String = OWNER_ID
}