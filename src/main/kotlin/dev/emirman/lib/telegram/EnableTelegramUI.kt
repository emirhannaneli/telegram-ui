package dev.emirman.lib.telegram

import dev.emirman.lib.telegram.command.TelegramCommandRegistrar
import dev.emirman.lib.telegram.config.TelegramBotProperties
import dev.emirman.lib.telegram.config.TelegramUIConfig
import dev.emirman.lib.telegram.handler.TelegramCommandHandler
import org.springframework.context.annotation.Import

@Import(
    TelegramCommandRegistrar::class,
    TelegramBotProperties::class,
    TelegramUIConfig::class,
    TelegramCommandHandler::class
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnableTelegramUI(
    val basePackages: Array<String> = [],
)
