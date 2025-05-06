package dev.emirman.lib.telegram.test

import dev.emirman.lib.telegram.EnableTelegramUI
import dev.emirman.lib.telegram.config.TelegramBotProperties
import dev.emirman.lib.telegram.test.command.TestFirstCommand
import dev.emirman.lib.telegram.test.command.TestSecondCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@EnableTelegramUI(
    basePackages = ["dev.emirman.lib.telegram.test"]
)
@SpringBootTest(
    properties = ["spring.profiles.active=test"],
    classes = [TelegramUITests::class],
)
@ExtendWith(SpringExtension::class)
class TelegramUITests {

    @Autowired
    lateinit var properties: TelegramBotProperties

    private val client = mock(TelegramClient::class.java)
    private lateinit var firstCommand: TestFirstCommand
    private lateinit var secondCommand: TestSecondCommand

    @BeforeEach
    fun setup() {
        firstCommand = TestFirstCommand(client)
        secondCommand = TestSecondCommand(client)
    }

    @Test
    fun `test telegram bot properties`() {
        assert(properties.bot.token != null) { "Bot token is null" }
    }

    @Test
    fun `test telegram bot first command`() {
        val chatId = 123456789L

        firstCommand.action().invoke(chatId)

        verify(client).execute(
            argThat<SendMessage> {
                it.chatId == chatId.toString() && it.text == "Executing First Command"
            }
        )
    }

    @Test
    fun `test telegram bot second command`() {
        val chatId = 123456789L

        secondCommand.action().invoke(chatId)

        verify(client).execute(
            argThat<SendMessage> {
                it.chatId == chatId.toString() && it.text == "Executing Second Command"
            }
        )
    }

}