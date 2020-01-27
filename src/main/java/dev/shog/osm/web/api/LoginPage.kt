package dev.shog.osm.web.api

import dev.shog.osm.web.api.response.INVALID_ARGUMENTS
import dev.shog.osm.web.api.response.Response
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

/**
 * Login.
 */
fun Routing.loginPage() {
    post("/login") {
        val params = call.parameters

        val username = params["username"]
        val password = params["password"]

        if (username == null || password == null) {
            call.respond(HttpStatusCode.BadRequest, INVALID_ARGUMENTS)
            return@post
        }

        val db = MySql.createConnection("mysql")

        val prepared = db.prepareStatement("SELECT * FROM \"user\" WHERE username = ?")

        prepared.setString(1, username)

        val rs = prepared.executeQuery()

        if (!rs.next())
            call.respond(HttpStatusCode.BadRequest, Response("Invalid username or password!"))
        else {
            when {
                rs.getInt("banned") == 1 ->
                    call.respond(HttpStatusCode.BadRequest, Response("You are banned!"))

                rs.getInt("banned") == 1 ->
                    call.respond(HttpStatusCode.BadRequest, Response("You are banned!"))

                rs.getInt("banned") == 1 ->
                    call.respond(HttpStatusCode.BadRequest, Response("You are banned!"))
            }
        }
    }
}