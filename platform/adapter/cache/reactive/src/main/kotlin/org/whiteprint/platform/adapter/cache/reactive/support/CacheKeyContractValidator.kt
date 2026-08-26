package org.whiteprint.platform.adapter.cache.reactive.support

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.whiteprint.platform.core.cache.annotation.CacheEvict
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.annotation.Cached
import org.whiteprint.platform.core.cache.annotation.CachedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import java.lang.reflect.Method

/**
 * `@Cached` 와 `@CacheEvict` 가 **같은 키를 만드는지** 기동 시점에 대조한다.
 *
 * 이 검사가 없으면 불일치가 이렇게 나타난다: 컴파일 통과, 예외 없음, 요청 성공.
 * 다만 **무효화가 영영 안 걸려서** 사용자가 낡은 값을 계속 본다.
 * 둘이 보통 다른 클래스(조회 어댑터 / 커맨드 서비스)에 있어서 코드 리뷰로도 잘 안 잡힌다.
 * 그래서 조용한 실패를 **기동 실패**로 바꾼다.
 *
 * 대조 기준은 **키 이름의 집합**이다 — 값은 런타임에만 알 수 있고, 순서는 이름순 정렬이라 무의미하다.
 *
 * `@Cached` 만 있고 `@CacheEvict` 가 없는 prefix 는 정상이다(TTL 로만 만료시키는 캐시).
 */
class CacheKeyContractValidator(
    private val applicationContext: ApplicationContext,
): SmartInitializingSingleton {

    override fun afterSingletonsInstantiated() {
        val cachedKeys = mutableMapOf<String, MutableSet<List<String>>>()
        val evictKeys = mutableMapOf<String, MutableSet<List<String>>>()

        forEachBeanMethod { method ->
            method.getAnnotation(Cached::class.java)?.let { cached ->
                val names = CacheKeyResolver.resolveNames(method, CachedKey::class.java) { it.name }
                requireKeyDefined(method, names)
                cachedKeys.getOrPut(cached.prefix) { mutableSetOf() }.add(names)
            }
            method.getAnnotation(CacheEvict::class.java)?.let { evict ->
                val names = CacheKeyResolver.resolveNames(method, CacheEvictKey::class.java) { it.name }
                requireKeyDefined(method, names)
                evictKeys.getOrPut(evict.prefix) { mutableSetOf() }.add(names)
            }
        }

        cachedKeys.forEach { (prefix, cachedShapes) ->
            val evictShapes = evictKeys[prefix] ?: return@forEach
            if (cachedShapes != evictShapes) {
                throw CacheException(
                    policy = CachePolicy.CACHE_KEY_CONTRACT_MISMATCH,
                    attributes = mapOf(
                        "prefix" to prefix,
                        "cached" to cachedShapes.joinToString(" | "),
                        "evicted" to evictShapes.joinToString(" | "),
                    ),
                )
            }
        }

        log.debug("Cache key contract verified — prefixes={}", cachedKeys.keys)
    }

    /**
     * 키가 하나도 안 잡히는 경우를 여기서 잡는다. 런타임에 잡으면 **첫 호출 때** 터지는데,
     * 그게 배포 검증을 지나 운영에서 나올 수 있다.
     */
    private fun requireKeyDefined(method: Method, names: List<String>) {
        if (names.isEmpty()) {
            throw CacheException(
                policy = CachePolicy.NO_CACHE_KEY_DEFINED,
                attributes = mapOf("key" to "${method.declaringClass.simpleName}.${method.name}"),
            )
        }
    }

    /**
     * 빈 **타입**만 훑는다 — 인스턴스를 꺼내면 lazy 빈을 강제로 만들게 된다.
     * 프록시가 씌워진 빈은 `getType` 이 대상 타입을 돌려주므로 애노테이션이 그대로 보인다.
     */
    private fun forEachBeanMethod(action: (Method) -> Unit) {
        applicationContext.beanDefinitionNames.forEach { name ->
            val type = runCatching { applicationContext.getType(name) }.getOrNull() ?: return@forEach
            runCatching { type.methods }.getOrNull()?.forEach(action)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CacheKeyContractValidator::class.java)
    }

}
