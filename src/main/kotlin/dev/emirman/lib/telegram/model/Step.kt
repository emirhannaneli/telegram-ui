package dev.emirman.lib.telegram.model

import dev.emirman.lib.telegram.command.AbstractTelegramCommand

data class Step(
    val name: String,
    val action: (Long) -> Any? = {},
    val data: String? = null,
    var next: Step? = null,
    var prev: Step? = null,
    val index: Int = 0
) : AbstractTelegramCommand() {

    constructor(name: String, command: AbstractTelegramCommand) : this(
        name = command.javaClass.simpleName,
        action = command.action(),
        data = command.data(),
    )

    override fun action(): (Long) -> Any? {
        return action
    }
}