package dev.shog.osm.web.api

import dev.shog.osm.web.URLS
import dev.shog.osm.web.api.response.INVALID_ARGUMENTS
import dev.shog.osm.web.api.response.Response
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.apache.commons.codec.digest.DigestUtils
import java.sql.Connection

/**
 * Register a new user.
 */
fun Routing.registerPage() {
    post("/register") {
        val params = call.receiveParameters()

        val email = params["email"]
        val username = params["username"]
        val password = params["password"]

        val db = MySql.createConnection("mysql")

        val cloak = Pair(username, "https://www.oldschoolminecraft.com/getimg?key=BugTracker")

        when {
            email == null || username == null || password == null ->
                call.respond(HttpStatusCode.BadRequest, INVALID_ARGUMENTS)

            !username.matches(Regex("/[^A-Za-z0-9_]/")) ->
                call.respond(HttpStatusCode.BadRequest, Response("Username contains invalid characters."))

            username.length < 3 ->
                call.respond(HttpStatusCode.BadRequest, Response("Username is too short."))

            username.length > 16 ->
                call.respond(HttpStatusCode.BadRequest, Response("Username is too long."))

            emailExists(db, email) ->
                call.respond(HttpStatusCode.BadRequest, Response("That email already exists!"))

            usernameExists(db, email) ->
                call.respond(HttpStatusCode.BadRequest, Response("That email already exists!"))

            else -> {
                createUserRecord(db, email, username, password)
                val vKey = randomString()
                createVerificationRecord(db, email, vKey)

                val link = "${URLS.api}/verify?email=$email&vkey=$vKey"
                val body = "Thank you for registering on Old School Minecraft!\n\nPlease verify your account using the link below.\n\n$link" +
                        "\n\nIf you received this email by mistake, you can delete it form our system using the following link.\n\n${URLS.api}/unsubscribe?email=$email"

                val headers = arrayListOf("Verify your OSM account", body, "From: noreply@oldschoolminecraft.com")

                // TODO send email

                call.respond(Response())
            }
        }

    }
}

/**
 * If the email already exists in the database.
 *
 * @param db The existing database connection.
 * @param email The requested email.
 */
fun emailExists(db: Connection, email: String): Boolean {
    val prepared = db.prepareStatement("SELECT * FROM \"user\" WHERE email = ?")

    prepared.setString(1, email)

    val rs = prepared.executeQuery()

    return rs.next()
}

/**
 * If the username already exists in the database.
 *
 * @param db The existing database connection.
 * @param username The requested username.
 */
fun usernameExists(db: Connection, username: String): Boolean {
    val prepared = db.prepareStatement("SELECT * FROM \"user\" WHERE username = ?")

    prepared.setString(1, username)

    val rs = prepared.executeQuery()

    return rs.next()
}

/**
 * Create a user's record in the database.
 *
 * @param db The existing database connection.
 * @param email The email the user wants.
 * @param username The username the user wants.
 * @param password The password the user wants, with a SHA-256 hex.
 * @param verified If the user's email is verified.
 * @param banned If the user is banned.
 */
fun createUserRecord(db: Connection, email: String, username: String, password: String, verified: Int = 0, banned: Int = 0) {
    val prepared = db.prepareStatement("INSERT INTO \"user\" (email, username, password, verified, banned) VALUES (?, ?, ?, ?, ?)")

    prepared.setString(1, email)
    prepared.setString(2, username)
    prepared.setString(3, DigestUtils.sha256Hex(password))
    prepared.setInt(4, verified)
    prepared.setInt(5, banned)

    prepared.executeUpdate()
}

/**
 * Get a random string with [length].
 */
fun randomString(length: Int = 32): String {
    val chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray()
    var str = ""

    for (i in 0..length) {
        str += chars.random()
    }

    return str
}

/**
 * Create a verification record.
 *
 * @param db The existing database connection.
 * @param email The user's email, to identify them.
 * @param vKey The key used to verify
 */
fun createVerificationRecord(db: Connection, email: String, vKey: String) {
    val prepared = db.prepareStatement("INSERT INTO verification (email, vkey) VALUES (?, ?)")

    prepared.setString(1, email)
    prepared.setString(2, vKey)

    prepared.executeUpdate()
}