package org.whiteprint.platform.infra.persistence.jpa.entity

import org.whiteprint.platform.core.domain.policy.PersistenceException
import org.whiteprint.platform.core.domain.policy.PersistencePolicy
import java.io.Serializable

/**
 * 새로 만든 엔티티에 식별자를 부여한다. 애플리케이션이 id를 직접 할당하는 규약(TSID 등)이라
 * 생성 경로에서 이 호출을 빠뜨리면 `_id`가 null인 채 persist까지 흘러간다.
 *
 * **[id]는 non-null이다.** 예전에는 nullable을 받아 null이면 조용히 아무 일도 하지 않았는데,
 * 부른 쪽은 할당했다고 믿고 진행하다가 한참 뒤 Hibernate의 식별자 예외로 터졌다.
 * 진짜로 "있으면 할당"이 필요한 자리가 생기면 호출부에서 `id?.let { entity.withId(it) }`로 명시할 것 —
 * 그 의도가 코드에 드러나야 한다.
 *
 * 호출 자체를 잊는 것은 여전히 막지 못한다. 그걸 막으려면 식별자가 생성자 파라미터여야 한다.
 */
fun <E: BaseEntity<ID>, ID: Serializable> E.withId(id: ID): E {
    this.assignId(id)
    return this
}

inline fun <reified E : BaseEntity<*>> E?.ensureExistsOrThrow(rootId: Any): E {
    return this ?:
        throw PersistenceException(
            policy = PersistencePolicy.INTEGRITY_VIOLATION,
            attributes = mapOf(
                "targetName" to (E::class.simpleName ?: "UnknownEntity"),
                "rootId" to rootId.toString()
            )
        )
}