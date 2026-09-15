package org.whiteprint.platform.core.lock.context

import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey

/**
 * 현재 스레드가 쥐고 있는 분산 락. `@DistributedLock` 애스펙트가 획득 직후 넣고 해제할 때 뺀다.
 *
 * 펜싱 검사(`@FencingGuarded` 엔티티 쓰기)가 여기서 토큰을 읽고, 애스펙트가 재진입 여부를 판정한다.
 *
 * ⚠ **스레드 로컬**이다. 락 구간 안에서 다른 스레드(비동기 실행기·코루틴 디스패처)로 넘긴 작업에는 보이지 않아
 * 그 쓰기는 펜싱 검사를 건너뛴다.
 */
object LockContext {

    private val held = ThreadLocal<ArrayDeque<LockHandle>>()

    fun push(lock: LockHandle) {
        val stack = held.get() ?: ArrayDeque<LockHandle>().also(held::set)
        stack.addLast(lock)
    }

    fun pop(lock: LockHandle) {
        val stack = held.get() ?: return
        val index = stack.lastIndexOf(lock)
        if (index >= 0) stack.removeAt(index)
        if (stack.isEmpty()) held.remove()
    }

    /** 현재 스레드가 [key] 락을 이미 쥐고 있는지 — 재진입 판정. */
    fun isHeld(key: LockKey): Boolean = held.get()?.any { it.key == key } == true

    /**
     * 펜싱 검사에 쓸 토큰. 락을 쥐고 있지 않으면 null(검사 건너뜀).
     *
     * 여러 락을 겹쳐 쥐었으면 **가장 오래된(가장 작은) 토큰**을 쓴다 — 토큰은 전역 카운터라 바깥 락일수록 작다.
     * 새 토큰을 쓰면 바깥 락을 이미 잃은 옛 보유자가 안쪽에서 새로 잡은 락 덕분에 검사를 통과해버린다.
     */
    fun currentFencingToken(): Long? = held.get()?.minOfOrNull { it.fencingToken }

}
