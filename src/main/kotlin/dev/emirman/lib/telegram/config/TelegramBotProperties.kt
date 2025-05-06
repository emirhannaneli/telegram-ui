package dev.emirman.lib.telegram.config

import org.jetbrains.annotations.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring.telegram")
open class TelegramBotProperties {
    val bot: Bot = Bot()

    @Configuration
    @ConfigurationProperties(prefix = "spring.telegram.bot")
    open class Bot {
        @get:NotNull
        var token: String? = null
    }
}