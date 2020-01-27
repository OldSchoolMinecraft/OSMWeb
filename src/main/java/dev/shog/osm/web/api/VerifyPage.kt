package dev.shog.osm.web.api

import dev.shog.osm.web.api.response.INVALID_ARGUMENTS
import dev.shog.osm.web.api.response.Response
import dev.shog.osm.web.data.Session
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

/**
 * The verify page.
 * This is used to verify a user's email.
 */
fun Routing.verifyPage() {
    post("/verify") {
        val db = MySql.createConnection("accounts")

        val params = call.receiveParameters()
        val email = params["email"]
        val vKey = params["vkey"]

        if (vKey == null || email == null) {
            call.respond(HttpStatusCode.BadRequest, INVALID_ARGUMENTS)
            return@post
        }

        val vReq = getVerificationRecord(db, email)
        val eReq = getUserRecord(db, email)

        when {
            vReq == null -> {
                call.respond(HttpStatusCode.BadRequest, Response("Verification record missing."))
                return@post
            }

            eReq == null -> {
                call.respond(HttpStatusCode.BadRequest, Response("User record missing."))
                return@post
            }

            eReq != email || vKey != vReq.second || email != vReq.first -> {
                call.respond(HttpStatusCode.BadRequest, Response("Data mismatch."))
                return@post
            }

            else -> {
                val prepared = db.prepareStatement("UPDATE \"user\" SET verified = ? WHERE email = ?")

                prepared.setInt(1, 1)
                prepared.setString(2, email)

                prepared.executeUpdate()

                deleteOld(db, email)

                val ses = call.sessions.get<Session>()

                if (ses != null)
                    call.sessions.set(Session(ses.loggedIn, ses.logInDate, ses.username, email, true))
                else call.sessions.clear<Session>()

                call.respond(Response())
            }
        }
    }
}

/**
 * Get a user's data.
 *
 * @param db The existing database connection.
 * @param email The user's email, to identify them.
 */
fun getUserRecord(db: Connection, email: String): String? {
    val prepared = db.prepareStatement("SELECT * from \"user\" WHERE email = ?")

    prepared.setString(1, email)

    val re = prepared.executeQuery()

    return if (re.next()) email else null
}

/**
 * Get the verification record.
 *
 * @param db The existing database connection.
 * @param email The user's email, to identify them.
 */
suspend fun getVerificationRecord(db: Connection, email: String): Pair<String, String>? {
    val prepared = db.prepareStatement("SELECT * FROM verification WHERE email = ?")

    prepared.setString(1, email)

    val rs = withContext(Dispatchers.Unconfined) { prepared.executeQuery() }

    return if (rs.next()) {
        Pair(email, rs.getString("vkey"))
    } else null
}

/**
 * Delete old verification? I don't really know what this does but it's in the code.
 *
 * @param db The existing database connection.
 * @param email The user's email, to identify them.
 */
suspend fun deleteOld(db: Connection, email: String) {
    val prepared = db.prepareStatement("DELETE FROM verification WHERE email = ?")

    prepared.setString(1, email)

    withContext(Dispatchers.Unconfined) { prepared.executeUpdate() }
}