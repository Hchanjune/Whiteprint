package com.hc.user.query.command

import com.hc.user.command.TestService
import io.github.hchanjune.operationresult.core.providers.OperationListener
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.jvm.java

@SpringBootTest
@ImportAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ]
)
class Test {

    @Autowired
    private lateinit var testService: TestService

    @Test
    fun proxyCheck() {
        println("isAopProxy = " + AopUtils.isAopProxy(testService))
        println("targetClass = " + AopUtils.getTargetClass(testService).name)
    }


    @Test
    fun test() {
        val result = testService.test()
        result.context.issuer
    }

    @Autowired lateinit var ctx: ApplicationContext

    @Test
    fun aspectBeanPresence() {
//        println(ctx.getBeansOfType(MetricsRecorder::class.java).keys)
//        println(ctx.getBeansOfType(MeterRegistry::class.java).keys)
//        println(ctx.getBeansOfType(MetricsEnricher::class.java).keys)
        println(ctx.getBeansOfType(OperationListener::class.java).keys)
    }


}