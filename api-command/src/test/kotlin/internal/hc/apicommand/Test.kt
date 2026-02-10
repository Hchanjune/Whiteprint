package internal.hc.apicommand

import io.github.hchanjune.operationresult.webmvc.aop.OperationServiceAspect
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

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
        println(result)
    }

    @Autowired lateinit var ctx: ApplicationContext

    @Test
    fun aspectBeanPresence() {
        println("OperationServiceAspect beans = " + ctx.getBeanNamesForType(OperationServiceAspect::class.java).toList())
    }


}