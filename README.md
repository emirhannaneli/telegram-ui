# Telegram Step-by-Step Command Workflow Library (Telegram UI)

This library is a Spring Boot-based extension that simplifies building step-by-step, button-driven command workflows for
Telegram bots. Instead of using multiple `/command`s, it enhances the user experience by navigating users through inline
buttons in a sequential manner.

## 🚀 Features

* Define sequential command chains in Telegram bots
* Guide users step-by-step using inline buttons
* Session-based state handling for form-like flows
* Annotation-based command configuration with `@TelegramCommand`
* Customizable command behavior with `action()`, `category()`, and `data()` methods

## 🔧 Installation

### Gradle

```kotlin
repositories {
    maven {
        url = uri("https://repo.emirman.dev")
    }
}

dependencies {
    implementation("dev.emirman.lib:telegram-ui:1.0.0")
}
```

## 📦 Basic Structure

### 1. Defining a Command

```kotlin
@TelegramCommand(
    name = "FirstCommand",
    next = TestSecondCommand::class,
    cancellable = true
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
```

### 2. Command Execution

```kotlin
@TelegramCommand(
    name = "SecondCommand",
)
class TestSecondCommand(val client: TelegramClient) : AbstractTelegramCommand() {
    override fun action(): (Long) -> Any? {
        return {
            val message = SendMessage.builder()
                .text("Executing Second Command")
                .chatId(it)
                .build()
            client.execute(message)
        }
    }
}
```

## 📘 Example Use Case

A user registration flow:

1. User initiates with `/start`
2. Clicks the "Register" button
3. Bot asks for name, surname, and email in sequence
4. Each step is represented as a `TelegramCommand`
5. User inputs are saved in session and used in the next step

## 📍 Notes

* Each command's next step is defined with `next`
* Commands can be marked as cancellable (`cancellable = true`)
* Inline button interaction and session tracking are integrated with Telegram Bots API and Spring Context
