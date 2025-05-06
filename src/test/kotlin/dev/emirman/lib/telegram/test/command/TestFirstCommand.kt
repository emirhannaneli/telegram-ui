package dev.emirman.lib.telegram.test.command

import dev.emirman.lib.telegram.command.TelegramCommand
import dev.emirman.lib.telegram.command.AbstractTelegramCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@TelegramCommand(
    name = "FirstCommand",
    next = TestSecondCommand::class,
    cancellable =  true
)
class TestFirstCommand(val client: TelegramClient) : AbstractTelegramCommand() {
    override fun action(): (Long) -> Any? {
        return {
            val message = SendMessage.builder()
                .text("Executing First Command")
                .chatId(it)
                .build()
            client.execute(message)
        }
    }
}