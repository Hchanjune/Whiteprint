package org.whiteprint.platform.adapter.cache.reactive.aspect

import io.github.hchanjune.omk.core.provider.SpanIdProvider
import kotlinx.coroutines.runBlocking
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.whiteprint.platform.core.cache.annotation.Deduplicated
import org.whiteprint.platform.core.cache.annotation.DeduplicatedKey
import org.whiteprint.platform.core.cache.annotation.DuplicateAction
import org.whiteprint.platform.core.cache.operation.ReactiveValueOperations
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeduplicatedAspectTest {

    open class Recorder {
        var skipCalls = 0
        var throwCalls = 0

        @Deduplicated(prefix = "test:skip:v1", onDuplicate = DuplicateAction.SKIP)
        open suspend fun skipOnDuplicate(@DeduplicatedKey id: String) {
            skipCalls++
        }

        @Deduplicated(prefix = "test:throw:v1")
        open suspend fun throwOnDuplicate(@DeduplicatedKey id: String) {
            throwCalls++
        }
    }

    private val seen = HashSet<String>()

    /** suspend 함수도 JVM 에서는 Continuation 을 더 받는 일반 메서드다 — 값을 바로 돌려주면 중단 없이 끝난다. */
    private val valueOperations = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ReactiveValueOperations::class.java),
    ) { _, method, args ->
        check(method.name.startsWith("setIfAbsentWithTtl")) { "unexpected call: ${method.name}" }
        seen.add(args[0].toString())
    } as ReactiveValueOperations

    private val provider = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ReactiveCacheProvider::class.java),
    ) { _, method, _ ->
        check(method.name == "getValue") { "unexpected call: ${method.name}" }
        valueOperations
    } as ReactiveCacheProvider

    private val target = Recorder()

    private val proxied: Recorder = AspectJProxyFactory(target).run {
        isProxyTargetClass = true
        addAspect(DeduplicatedAspect(provider, SpanIdProvider { "span" }))
        getProxy()
    }

    @Test
    fun `SKIP 은 첫 호출만 실행하고 중복은 예외 없이 건너뛴다`() = runBlocking {
        proxied.skipOnDuplicate("a")
        proxied.skipOnDuplicate("a")

        assertEquals(1, target.skipCalls)
    }

    @Test
    fun `SKIP 은 키가 다르면 각각 실행한다`() = runBlocking {
        proxied.skipOnDuplicate("a")
        proxied.skipOnDuplicate("b")

        assertEquals(2, target.skipCalls)
    }

    @Test
    fun `기본값은 중복이면 DUPLICATE_REQUEST 를 던진다`() = runBlocking {
        proxied.throwOnDuplicate("a")

        val exception = assertFailsWith<CacheException> { proxied.throwOnDuplicate("a") }

        assertEquals(CachePolicy.DUPLICATE_REQUEST, exception.policy)
        assertEquals(1, target.throwCalls)
    }
}
