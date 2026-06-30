package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource
import org.springframework.data.repository.config.RepositoryConfigurationDelegate
import org.springframework.data.util.Streamable
import org.whiteprint.platform.infra.persistence.jpa.repository.OptimizedJpaRepository

internal class JpaRepositoryRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private lateinit var environment: Environment
    private lateinit var resourceLoader: ResourceLoader

    override fun setEnvironment(environment: Environment) { this.environment = environment }
    override fun setResourceLoader(resourceLoader: ResourceLoader) { this.resourceLoader = resourceLoader }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val additionalPackages: List<String> = Binder.get(environment)
            .bind(
                "adapter.persistence.jpa.options.entity-packages-to-scan",
                Bindable.listOf(String::class.java)
            )
            .orElse(emptyList()) ?: emptyList()

        val baseMetadata = AnnotationMetadata.introspect(DefaultRepositoryConfiguration::class.java)
        val configSource = object : AnnotationRepositoryConfigurationSource(
            baseMetadata,
            EnableJpaRepositories::class.java,
            resourceLoader,
            environment,
            registry,
            null,
        ) {
            override fun getBasePackages(): Streamable<String> {
                val parentPackages = super.getBasePackages()
                return Streamable.of(java.util.function.Supplier {
                    java.util.stream.Stream.concat(parentPackages.stream(), additionalPackages.stream())
                })
            }
        }

        RepositoryConfigurationDelegate(configSource, resourceLoader, environment)
            .registerRepositoriesIn(registry, JpaRepositoryConfigExtension())
    }

    @EnableJpaRepositories(
        basePackages = ["org.whiteprint"],
        repositoryBaseClass = OptimizedJpaRepository::class,
    )
    private class DefaultRepositoryConfiguration
}
