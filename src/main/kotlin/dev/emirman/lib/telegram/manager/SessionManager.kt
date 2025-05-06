package dev.emirman.lib.telegram.manager

import dev.emirman.lib.telegram.manager.SessionManager.Companion.data
import dev.emirman.lib.telegram.model.SessionData

/**
 * Manages user sessions for the Telegram bot.
 *
 * This class provides functionality to store and retrieve session data
 * associated with user IDs. It uses a companion object to maintain a
 * centralized session storage.
 */
class SessionManager {

    companion object {
        // A map to store session data, where the key is the user ID and the value is the session data.
        private val sessions = mutableMapOf<Long, SessionData<*>>()

        /**
         * Retrieves the session data for a given user ID.
         *
         * If no session data exists for the given ID, a new `SessionData` instance is created and returned.
         *
         * @param id The user ID for which the session data is retrieved.
         * @return The session data associated with the user ID.
         * @throws ClassCastException if the stored session data cannot be cast to the expected type.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> data(id: Long): SessionData<T> {
            return sessions[id] as SessionData<T>?
                ?: SessionData()
        }

        /**
         * Stores session data for a given user ID.
         *
         * This method associates the provided session data with the specified user ID.
         *
         * @param id The user ID for which the session data is stored.
         * @param data The session data to be stored.
         */
        fun <T> data(id: Long, data: SessionData<T>) {
            sessions[id] = data
        }
    }
}