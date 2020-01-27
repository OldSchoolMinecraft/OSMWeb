package dev.shog.osm.web.api

import dev.shog.osm.web.api.response.Response
import dev.shog.osm.web.data.Session
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.sessions

/**
 * For a user to logout.
 */
fun Routing.logoutPage() {
    post("/logout") {
        call.sessions.clear<Session>()
        call.respond(Response())
    }
}