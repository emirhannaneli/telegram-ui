package dev.emirman.lib.telegram.command

/**
 * Abstract base class for defining Telegram commands.
 *
 * This class provides a structure for creating Telegram commands with an action,
 * optional data, and a category. It also includes a method to execute the command.
 */
abstract class AbstractTelegramCommand {

    /**
     * Defines the action to be performed by the command.
     *
     * @return A lambda function that takes a user ID (`id`) as a parameter and returns any result.
     */
    abstract fun action(): (id: Long) -> Any?

    /**
     * Provides optional data associated with the command.
     *
     * This method can be overridden to return specific data of type `T`.
     *
     * @return The data of type `T`, or `null` if no data is provided.
     */
    open fun <T> data(): T? = null

    /**
     * Specifies the category of the command.
     *
     * This method can be overridden to return a custom category. By default, it returns "default".
     *
     * @return The category of the command as a `String`.
     */
    open fun category(): String = "default"

    /**
     * Executes the command by invoking its action with the given user ID.
     *
     * @param id The user ID for which the command is executed.
     */
    fun execute(id: Long) {
        action().invoke(id)
    }
}