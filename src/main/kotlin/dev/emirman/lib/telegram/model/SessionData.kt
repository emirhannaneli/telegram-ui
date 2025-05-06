package dev.emirman.lib.telegram.model

data class SessionData<T>(
    var data: T? = null,
    var awaiting: Step? = null,
    var completed: Boolean = false,
)