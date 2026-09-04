package org.whiteprint.platform.core.kernel.identifier

import java.util.concurrent.atomic.AtomicLong

/**
 * 시간 순서를 가지는 64비트 식별자. `[Timestamp 42][NodeId 10][Sequence 12]`.
 *
 * 마지막 12비트가 **밀리초당 시퀀스**다(예전에는 랜덤이었다). 랜덤이던 시절에는 같은 밀리초에
 * 여러 건을 만들면 4096칸을 랜덤으로 뽑는 셈이라 생일 문제로 충돌했다 —
 * 실측하면 1000개 연속 생성에 120건 안팎이 겹쳤고, 이는 **단일 인스턴스에서도** 나는 문제였다.
 * (알림 팬아웃처럼 한 청크에 수백~수천 건을 만드는 경로에서 PK 충돌로 드러난다.)
 *
 * 시퀀스로 바꾸면서 얻은 것:
 * - **한 프로세스 안에서는 충돌이 구조적으로 불가능**하다. 밀리초당 4096개를 소진하면 다음 밀리초까지 기다린다.
 * - 같은 밀리초 안에서도 생성 순서대로 증가한다 — keyset 페이지네이션이 기대는 정렬성이 강해진다.
 *
 * ⚠ **인스턴스 간 유일성은 [nodeId] 가 책임진다.** 여러 인스턴스가 같은 nodeId 로 돌면
 * 같은 밀리초·같은 시퀀스가 나올 수 있다. 배정은 이 클래스의 관심사가 아니다.
 */
object TsidGenerator {
    // 2026-01-01 00:00:00 UTC
    private const val CUSTOM_EPOCH = 1767225600000L
    private const val NODE_ID_BITS = 10    // 최대 1024개 노드
    private const val SEQUENCE_BITS = 12   // 밀리초당 최대 4096개
    private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1
    private const val MAX_NODE_ID = (1L shl NODE_ID_BITS) - 1

    /**
     * `[lastTimestamp 42][lastSequence 12]` 를 한 Long 에 담아 CAS 로 한 번에 옮긴다 —
     * 둘을 따로 두면 그 사이에 끼어든 스레드가 같은 (timestamp, sequence) 쌍을 받을 수 있다.
     */
    private val state = AtomicLong(0)

    /**
     * 무인자 [generate] 가 쓰는 노드 번호. 프로세스마다 한 번, 부팅 시 설정된다.
     *
     * **설정을 강제하는 것은 이 클래스가 아니라 어댑터다**(`adapter/identifier`). 여기서 실패시키면
     * 커널만 쓰는 단위 테스트까지 설정을 요구하게 되고, 응답 id 같은 일회성 생성까지 막힌다.
     * 어댑터가 기동 시점에 없으면 못 뜨게 막으므로, 애플리케이션이 도는 동안엔 항상 설정돼 있다.
     */
    @Volatile
    private var configuredNodeId: Long = 0

    /** 부팅 시 1회. 스프링 배선은 `adapter/identifier` 가 한다. */
    fun configure(nodeId: Long) {
        require(nodeId in 0..MAX_NODE_ID) { "nodeId 는 0..$MAX_NODE_ID 범위여야 한다: $nodeId" }
        configuredNodeId = nodeId
    }

    /** 설정된 노드 번호로 생성한다. 설정 전이면 0 이다 — 그 강제는 어댑터의 몫이다. */
    fun generate(): Long = generate(configuredNodeId)

    /**
     * String 기반 nodeId: 호스트명이나 IP 등을 해싱하여 10비트 숫자로 변환.
     *
     * ⚠ 해시는 **조용히 겹친다** — 1024칸에 서로 다른 이름 여럿을 넣으면 충돌 확률이 무시할 수준이 아니다.
     * 유일성이 중요한 자리에는 숫자를 명시적으로 배정해 [generate] 에 직접 넘길 것.
     */
    fun generate(nodeId: String): Long = generate(nodeId.hashCode().toLong() and MAX_NODE_ID)

    /** Long 기반 nodeId: 직접 숫자를 주입받아 ID 생성. */
    fun generate(nodeId: Long): Long {
        val node = (nodeId and MAX_NODE_ID) shl SEQUENCE_BITS

        while (true) {
            val previous = state.get()
            val previousTimestamp = previous ushr SEQUENCE_BITS
            val previousSequence = previous and MAX_SEQUENCE

            val now = System.currentTimeMillis() - CUSTOM_EPOCH

            val timestamp: Long
            val sequence: Long
            if (now > previousTimestamp) {
                timestamp = now
                sequence = 0
            } else {
                // now < previousTimestamp 는 시계 역행이다. 뒤로 가면 이미 발급한 id 와 겹치므로
                // 시각을 되돌리지 않고 직전 밀리초에서 시퀀스를 이어 쓴다.
                timestamp = previousTimestamp
                sequence = previousSequence + 1
                if (sequence > MAX_SEQUENCE) {
                    // 이 밀리초를 다 썼다 — 다음 밀리초가 올 때까지 기다린 뒤 다시 시도한다.
                    awaitNextMillis(previousTimestamp)
                    continue
                }
            }

            if (state.compareAndSet(previous, (timestamp shl SEQUENCE_BITS) or sequence)) {
                return (timestamp shl (NODE_ID_BITS + SEQUENCE_BITS)) or node or sequence
            }
            // CAS 실패 = 다른 스레드가 먼저 가져갔다. 다시 읽고 재시도한다.
        }
    }

    private fun awaitNextMillis(currentTimestamp: Long) {
        while (System.currentTimeMillis() - CUSTOM_EPOCH <= currentTimestamp) {
            Thread.onSpinWait()
        }
    }
}
