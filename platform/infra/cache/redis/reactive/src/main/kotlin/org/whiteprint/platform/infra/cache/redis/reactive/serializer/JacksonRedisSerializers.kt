package org.whiteprint.platform.infra.cache.redis.reactive.serializer

import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import tools.jackson.module.kotlin.KotlinModule

/**
 * 캐시 값 직렬화기(reactive). servlet 쪽 `JacksonRedisSerializers` 와 **같은 형식을 만들어야 한다** —
 * 두 스택이 같은 Redis 를 보는 배포에서 형식이 갈리면 서로의 캐시를 못 읽는다.
 *
 * ## 왜 직접 만드나
 * `RedisSerializer.json()` 이 조립하는 ObjectMapper 에는 **KotlinModule 이 명시적으로 붙어 있지 않다.**
 * 지금은 Jackson 3 의 모듈 자동 발견 덕에 클래스패스에 `jackson-module-kotlin` 이 있으면 우연히 동작하지만,
 * 그 의존이 빠지는 순간 **주 생성자만 있는 Kotlin data class 를 캐시에서 꺼낼 때** 터진다.
 * 게다가 그 실패는 **넣을 때가 아니라 두 번째 조회부터** 나타나서 원인을 찾기 어렵다.
 *
 * ## 기본값은 그대로 유지한다
 * `RedisSerializer.json()` 은 `enableSpringCacheNullValueSupport()` + `enableUnsafeDefaultTyping()` 을 켠다.
 * **둘 다 그대로 둬야 한다** — default typing 을 끄면 저장된 값이 타입 정보를 잃어
 * 꺼낼 때 전부 `LinkedHashMap` 이 되고, 이미 저장된 캐시와 형식도 어긋난다.
 */
object JacksonRedisSerializers {

    fun json(): RedisSerializer<Any> =
        GenericJacksonJsonRedisSerializer.create { builder ->
            builder
                .enableSpringCacheNullValueSupport()
                .enableUnsafeDefaultTyping()
                .customize { mapperBuilder -> mapperBuilder.addModule(KotlinModule.Builder().build()) }
        }

}
