package dev.emirman.lib.telegram.config

import org.springframework.context.annotation.*
import org.springframework.core.type.AnnotatedTypeMetadata
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

/**
 * Configuration class for setting up Telegram UI components.
 *
 * This class provides the necessary beans and conditions to configure
 * a Telegram client using the provided bot properties.
 *
 * @property config The properties for configuring the Telegram bot.
 */
@Configuration
open class TelegramUIConfig(val config: TelegramBotProperties) {

    /**
     * Defines a bean for the Telegram client.
     *
     * This bean is only created if the `ClientCondition` is satisfied.
     *
     * @return An instance of `TelegramClient` using the OkHttp implementation.
     */
    @Bean
    @Conditional(ClientCondition::class)
    open fun telegramClient(): TelegramClient {
        val token = config.bot.token

        return OkHttpTelegramClient(token)
    }

    /**
     * Condition to determine whether the Telegram client bean should be created.
     *
     * This condition checks if a bean of type `TelegramClient` is already present
     * in the application context. If not, the condition is satisfied.
     */
    class ClientCondition : Condition {
        /**
         * Evaluates the condition to determine if the Telegram client bean should be created.
         *
         * @param context The condition context providing access to the application context.
         * @param metadata Metadata of the annotated component.
         * @return `true` if the Telegram client bean is not already present, `false` otherwise.
         */
        override fun matches(
            context: ConditionContext,
            metadata: AnnotatedTypeMetadata
        ): Boolean {
            val beanFactory = context.beanFactory ?: return false
            return !beanFactory.containsBean(TelegramClient::class.java.name)
        }
    }
}