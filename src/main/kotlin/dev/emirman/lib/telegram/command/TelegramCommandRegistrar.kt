package dev.emirman.lib.telegram.command

import dev.emirman.lib.telegram.EnableTelegramUI
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter

/**
 * A Spring configuration class that dynamically registers Telegram command beans.
 *
 * This class scans for classes annotated with `@TelegramCommand` within the specified base packages
 * and registers them as beans in the Spring application context.
 */
@Configuration
open class TelegramCommandRegistrar : ImportBeanDefinitionRegistrar {

    /**
     * Registers bean definitions for classes annotated with `@TelegramCommand`.
     *
     * @param importingClassMetadata Metadata of the importing class, used to retrieve annotation attributes.
     * @param registry The registry to register the bean definitions.
     */
    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry
    ) {
        // Retrieve the attributes of the @EnableTelegramUI annotation
        val attrs = importingClassMetadata.getAnnotationAttributes(EnableTelegramUI::class.java.name)

        // Extract the base packages to scan for Telegram commands
        val basePackages = attrs?.get("basePackages") as Array<*>

        // Create a scanner to find components annotated with @TelegramCommand
        val scanner = ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(AnnotationTypeFilter(TelegramCommand::class.java))
        }

        // Iterate over the base packages and register each found component as a bean
        basePackages.forEach { basePackage ->
            val candidate = scanner.findCandidateComponents(basePackage as String)
            candidate.forEach { candidate ->
                val className = candidate.beanClassName
                val clazz = Class.forName(className)
                val name = clazz.simpleName

                // Define the bean with singleton scope and autowiring by type
                val definition = GenericBeanDefinition().apply {
                    beanClass = clazz
                    autowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE
                    scope = BeanDefinition.SCOPE_SINGLETON
                }
                // Register the bean definition in the registry
                registry.registerBeanDefinition(name, definition)
            }
        }
    }
}