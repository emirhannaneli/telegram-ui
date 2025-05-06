package dev.emirman.lib.telegram.command

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * Annotation to define a Telegram command.
 *
 * This annotation is used to mark a class as a Telegram command and provides metadata
 * such as the command name, whether the command is cancellable, the next command in the sequence,
 * and the index of the command for ordering purposes.
 *
 * @property value The name of the command. This is an alias for `name`.
 * @property name The name of the command. This is an alias for `value`.
 * @property cancellable Indicates whether the command can be cancelled. Defaults to `false`.
 * @property next The class of the next `TelegramCommandModel` to be executed after this command.
 *                Defaults to `AbstractTelegramCommand::class`.
 * @property index The index of the command, used for ordering or prioritization. Defaults to 0.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TelegramCommand(
    @get:AliasFor("name")
    val value: String = "",
    @get:AliasFor("value")
    val name: String = "",
    val cancellable: Boolean = false,
    val next: KClass<out AbstractTelegramCommand> = AbstractTelegramCommand::class,
    val index: Int = 0,
)