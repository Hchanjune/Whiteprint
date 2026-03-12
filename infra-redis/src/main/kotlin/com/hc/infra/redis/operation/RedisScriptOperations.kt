package com.hc.infra.redis.operation

import org.springframework.data.redis.core.script.DefaultRedisScript

class RedisScriptOperations {

    companion object {
        private const val RELEASE_LOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1]
            then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """
    }


    fun releaseLock(key: String, owner: String): Boolean {
        val script = DefaultRedisScript<Long>().apply {
            setScriptText(RELEASE_LOCK_SCRIPT)
            resultType = Long::class.java
        }

        val result

    }

}