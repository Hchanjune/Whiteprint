package org.whiteprint.platform.infra.persistence.jpa.entity.fencing

import org.whiteprint.platform.core.lock.annotation.FencingGuarded
import org.whiteprint.platform.core.lock.context.LockContext
import org.whiteprint.platform.core.lock.policy.LockException
import org.whiteprint.platform.core.lock.policy.LockPolicy
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * `@FencingGuarded` 엔티티의 펜싱 토큰 검사·기록. `RootEntity` 의 `@PreUpdate`·`@PrePersist` 에서 부른다.
 *
 * ## 왜 원자적인가
 * 비교 대상 `lastFencingToken` 은 **이 트랜잭션이 읽은 값**이다. 읽은 뒤 다른 보유자가 먼저 썼다면 그 쓰기가
 * `@Version` 을 올렸으므로 이쪽 UPDATE 는 버전 충돌로 실패한다. 읽기 전에 썼다면 더 큰 토큰이 보여 여기서 거절된다.
 * 그래서 별도 조건부 UPDATE 없이 "읽은 토큰과 비교 → 기록"만으로 충분하다.
 *
 * `@PreUpdate` 에서 바꾼 필드도 같은 UPDATE 에 실린다 — `updatedAt` 이 이미 같은 방식으로 기록되고 있다.
 */
internal object FencingTokenGuard {

    private val guardedTypes = ConcurrentHashMap<Class<*>, Boolean>()

    private fun isGuarded(entity: RootEntity<*>): Boolean =
        guardedTypes.computeIfAbsent(entity.javaClass) { it.isAnnotationPresent(FencingGuarded::class.java) }

    /** 갱신 직전. 락 구간 밖이거나 대상 엔티티가 아니면 아무것도 하지 않는다. */
    fun checkAndStamp(entity: RootEntity<*>) {
        if (!isGuarded(entity)) return
        val token = LockContext.currentFencingToken() ?: return

        if (token < entity.lastFencingToken) {
            throw LockException(
                LockPolicy.FENCING_TOKEN_REJECTED,
                mapOf(
                    "entity" to entity.javaClass.simpleName,
                    "id" to entity.id.toString(),
                    "token" to token,
                    "lastToken" to entity.lastFencingToken,
                ),
            )
        }
        entity.lastFencingToken = token
    }

    /** 삽입 직전. 새 행이라 비교할 값이 없다 — 락 구간 안이면 토큰만 기록한다. */
    fun stampOnInsert(entity: RootEntity<*>) {
        if (!isGuarded(entity)) return
        val token = LockContext.currentFencingToken() ?: return
        entity.lastFencingToken = token
    }

}
