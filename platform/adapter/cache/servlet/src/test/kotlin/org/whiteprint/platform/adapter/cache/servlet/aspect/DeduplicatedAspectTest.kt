package org.whiteprint.platform.adapter.cache.servlet.aspect

import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.whiteprint.platform.core.cache.annotation.Deduplicated
import org.whiteprint.platform.core.cache.annotation.DeduplicatedKey
import org.whiteprint.platform.core.cache.annotation.DuplicateAction
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeduplicatedAspectTest {

    open class Recorder {
        var skipCalls = 0
        var throwCalls = 0

        @Deduplicated(prefix = "test:skip:v1", onDuplicate = DuplicateAction.SKIP)
        open fun skipOnDuplicate(@DeduplicatedKey id: String) {
            skipCalls++
        }

        @Deduplicated(prefix = "test:throw:v1")
        open fun throwOnDuplicate(@DeduplicatedKey id: String) {
            throwCalls++
        }
    }

    private val seen = HashSet<String>()

    private val valueOperations = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ValueOperations::class.java),
    ) { _, method, args ->
        check(method.name.startsWith("setIfAbsentWithTtl")) { "unexpected call: ${method.name}" }
        seen.add(args[0].toString())
    } as ValueOperations

    private val provider = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(CacheProvider::class.java),
    ) { _, method, _ ->
        check(method.name == "getValue") { "unexpected call: ${method.name}" }
        valueOperations
    } as CacheProvider

    private val target = Recorder()

    private val proxied: Recorder = AspectJProxyFactory(target).run {
        isProxyTargetClass = true
        addAspect(DeduplicatedAspect(provider, SpanIdProvider { "span" }))
        getProxy()
    }

    @Test
    fun `SKIP 은 첫 호출만 실행하고 중복은 예외 없이 건너뛴다`() {
        proxied.skipOnDuplicate("a")
        proxied.skipOnDuplicate("a")

        assertEquals(1, target.skipCalls)
    }

    @Test
    fun `기본값은 중복이면 DUPLICATE_REQUEST 를 던진다`() {
        proxied.throwOnDuplicate("a")

        val exception = assertFailsWith<CacheException> { proxied.throwOnDuplicate("a") }

        assertEquals(CachePolicy.DUPLICATE_REQUEST, exception.policy)
        assertEquals(1, target.throwCalls)
    }
}
