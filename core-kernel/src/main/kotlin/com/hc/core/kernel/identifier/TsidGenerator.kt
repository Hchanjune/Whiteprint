package com.hc.core.kernel.identifier

import kotlin.random.Random

object TsidGenerator {
    // 2026-01-01 00:00:00 UTC
    private const val CUSTOM_EPOCH = 1767225600000L
    private const val NODE_ID_BITS = 10   // 최대 1024개 노드
    private const val RANDOM_BITS = 12    // 밀리초당 최대 4096개 랜덤값

    /**
     * 기본 생성: nodeId를 0으로 사용
     */
    fun generate(): Long {
        return generate(0L)
    }

    /**
     * String 기반 nodeId: 호스트명이나 IP 등을 해싱하여 10비트 숫자로 변환
     */
    fun generate(nodeId: String): Long {
        // 해시코드의 음수 방지를 위해 bitwise AND 처리 (0~1023 사이로 고정)
        val numericNodeId = (nodeId.hashCode().toLong() and ((1L shl NODE_ID_BITS) - 1))
        return generate(numericNodeId)
    }

    /**
     * Long 기반 nodeId: 직접 숫자를 주입받아 ID 생성
     */
    fun generate(nodeId: Long): Long {
        val timestamp = System.currentTimeMillis() - CUSTOM_EPOCH

        // 12비트 범위(0~4095)의 랜덤값 생성
        val randomPart = Random.nextLong(0, 1L shl RANDOM_BITS)

        // [Timestamp (42bit)] [NodeId (10bit)] [Random (12bit)]
        return (timestamp shl (NODE_ID_BITS + RANDOM_BITS)) or
                ((nodeId and ((1L shl NODE_ID_BITS) - 1)) shl RANDOM_BITS) or
                randomPart
    }
}