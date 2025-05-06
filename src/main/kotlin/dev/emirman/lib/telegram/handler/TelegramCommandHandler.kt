package dev.emirman.lib.telegram.handler

import dev.emirman.lib.telegram.command.AbstractTelegramCommand
import dev.emirman.lib.telegram.command.TelegramCommand
import dev.emirman.lib.telegram.config.TelegramBotProperties
import dev.emirman.lib.telegram.manager.SessionManager
import dev.emirman.lib.telegram.model.SessionData
import dev.emirman.lib.telegram.model.Step
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Handles Telegram commands and manages their execution flow.
 *
 * This class integrates with the Spring application context and Telegram Bot API
 * to process user interactions, manage sessions, and execute commands.
 *
 * @property ctx The Spring application context used to retrieve beans and manage dependencies.
 * @property config The configuration properties for the Telegram bot.
 * @property client The Telegram client used to send messages and interact with the Telegram API.
 */
@Configuration
open class TelegramCommandHandler(
    val ctx: GenericApplicationContext,
    val config: TelegramBotProperties,
    val client: TelegramClient
) : SpringLongPollingBot {
    private val dataSet: MutableSet<CommandData> = HashSet()

    /**
     * Retrieves the bot token from the configuration.
     *
     * @return The bot token as a string.
     * @throws IllegalStateException if the bot token is not set in the configuration.
     */
    override fun getBotToken(): String {
        return config.bot.token ?: throw IllegalStateException("Bot token is not set")
    }

    /**
     * Provides the update consumer for processing incoming updates.
     *
     * @return A `LongPollingUpdateConsumer` to handle updates.
     */
    override fun getUpdatesConsumer(): LongPollingUpdateConsumer? {
        return LongPollingSingleThreadUpdateConsumer { update ->
            when {
                update.hasCallbackQuery() -> {
                    val id = update.callbackQuery.message.chat.id
                    val data = update.callbackQuery.data

                    val commandData = dataSet.find { it.name == data }
                        ?: return@LongPollingSingleThreadUpdateConsumer
                    val command = commandData.command

                    command.execute(id)

                    if (commandData.cancellable) {
                        val markup = InlineKeyboardMarkup.builder()
                            .keyboardRow(
                                InlineKeyboardRow().apply {
                                    add(InlineKeyboardButton("❌").apply {
                                        callbackData = "cancel"
                                    })
                                }
                            )
                            .build()

                        val message = SendMessage.builder()
                            .text("Please select a command:")
                            .chatId(id)
                            .replyMarkup(markup)
                            .build()

                        client.execute(message)
                    }

                    val step = Step(
                        commandData.name,
                        command.action(),
                        command.data(),
                        commandData.next,
                        commandData.prev,
                        commandData.index
                    )

                    SessionManager.data(id, SessionData<Properties>(awaiting = step))
                }

                update.hasMessage() && update.message.hasText() -> {
                    val id = update.message.chat.id
                    val data = update.message.text

                    if (data == "/start") {
                        startMenu(id)
                        return@LongPollingSingleThreadUpdateConsumer
                    } else if (data == "cancel") {
                        SessionManager.data(id, SessionData<Properties>(completed = true))
                        startMenu(id)
                        return@LongPollingSingleThreadUpdateConsumer
                    }

                    val session = SessionManager.data<Properties>(id)
                    if (!session.completed) {
                        val command = session.awaiting
                        val hasNext = command?.next == null
                        session.apply {
                            this.awaiting = command?.next
                            this.data = this.data?.apply {
                                setProperty(command?.name, data)
                            } ?: Properties().apply {
                                setProperty(command?.name, data)
                            }
                        }

                        command?.next?.execute(id)

                        session.completed = hasNext
                        SessionManager.data(id, session)
                    }
                }
            }
        }
    }

    /**
     * Displays the start menu to the user.
     *
     * @param id The chat ID of the user.
     */
    private fun startMenu(id: Long) {
        if (dataSet.isEmpty()) return

        val rows = dataSet
            .groupBy { it.category }
            .map { group ->
                val row = InlineKeyboardRow()
                row.addAll(
                    group.value
                        .filter { it.prev == null }
                        .map { command ->
                            InlineKeyboardButton(command.name).apply {
                                callbackData = command.name
                            }
                        })
                row
            }

        val markup = InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build()

        val message = SendMessage.builder()
            .text("Please select a command:")
            .chatId(id)
            .replyMarkup(markup)
            .build()

        client.execute(message)
    }

    init {
        val beans = ctx.getBeansWithAnnotation(TelegramCommand::class.java)
        beans.forEach { bean ->
            val annotation = bean.value::class.findAnnotation<TelegramCommand>()
            annotation?.let { annotation ->
                val command = bean.value as AbstractTelegramCommand

                load(annotation, command)
            }
        }
        buildStepChain()
    }

    /**
     * Loads a command and its metadata into the handler.
     *
     * @param annotation The `TelegramCommand` annotation of the command.
     * @param command The command instance to be loaded.
     */
    private fun load(
        annotation: TelegramCommand,
        command: AbstractTelegramCommand,
    ) {
        val name = annotation.name.ifBlank { annotation.value }
        val category = command.category()

        val next = if (annotation.next != AbstractTelegramCommand::class)
            buildStepChain(annotation.next)
        else null

        val data = CommandData(
            name = name,
            category = category,
            command = command,
            next = next,
            index = annotation.index,
            cancellable = annotation.cancellable
        )

        dataSet.add(data)
    }

    /**
     * Builds the step chain for all commands in the dataset.
     */
    private fun buildStepChain() {
        dataSet.forEach { data ->
            val next = dataSet.find { it.name == data.next?.name }
            next?.let {
                it.prev = Step(
                    name = data.name,
                    action = data.command.action(),
                    data = data.command.data(),
                    index = it.index
                )
            }
        }
    }

    /**
     * Recursively builds the step chain for a specific command class.
     *
     * @param clazz The class of the command to build the step chain for.
     * @param visited A set of visited command names to prevent infinite recursion.
     * @return The `Step` object representing the command's step chain.
     */
    private fun buildStepChain(
        clazz: KClass<out AbstractTelegramCommand>,
        visited: MutableSet<String> = mutableSetOf()
    ): Step? {
        val bean = runCatching { ctx.getBean(clazz.java) }.getOrNull() ?: return null
        val annotation = bean.javaClass.getAnnotation(TelegramCommand::class.java) ?: return null
        val name = annotation.name.ifBlank { annotation.value }

        if (visited.contains(name)) return null
        visited.add(name)

        val nextStep = if (annotation.next != AbstractTelegramCommand::class)
            buildStepChain(annotation.next, visited)
        else null

        return Step(
            name = name,
            action = bean.action(),
            data = bean.data(),
            next = nextStep,
            prev = null,
            index = annotation.index
        )
    }

    /**
     * Represents metadata and relationships for a Telegram command.
     *
     * @property name The name of the command.
     * @property category The category of the command.
     * @property command The command instance.
     * @property next The next step in the command chain.
     * @property prev The previous step in the command chain.
     * @property index The index of the command in the chain.
     */
    private class CommandData(
        val name: String,
        val category: String,
        val command: AbstractTelegramCommand,
        var next: Step? = null,
        var prev: Step? = null,
        val index: Int = 0,
        val cancellable: Boolean = false,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CommandData

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}