package com.hc.core.domain.identifier

import kotlin.random.Random

object TsidGenerator {
    private const val DEFAULT_NODE_ID = 0L
    private const val EPOCH = 0L

    fun generate(): Long {
        val timestampMs = System.currentTimeMillis() - EPOCH
        val randomPart = Random.nextLong(0, 4096)
        return (timestampMs shl 22) or (DEFAULT_NODE_ID shl 12) or randomPart
    }
}