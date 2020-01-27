package dev.shog.osm.web.data

/**
 * A session for users.
 */
data class Session(
        val loggedIn: Boolean,
        val logInDate: Long,
        val username: String,
        val email: String,
        val verified: Boolean
)