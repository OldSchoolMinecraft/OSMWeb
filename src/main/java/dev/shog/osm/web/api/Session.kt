package dev.shog.osm.web.api

import dev.shog.osm.web.api.response.Response
import dev.shog.osm.web.data.Session
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.sessions.get
import io.ktor.sessions.sessions

/**
 * Get session data.
 */
fun Routing.sessionPage() {
    get("/session") {
        val ses = call.sessions.get<Session>()

        if (ses == null)
            call.respond(HttpStatusCode.BadRequest, Response("NOT OK :("))
        else call.respond(Response(data = ses))
    }
}