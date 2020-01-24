package dev.shog.osm.web.api

import dev.shog.osm.web.api.response.INVALID_ARGUMENTS
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level

@KtorExperimentalLocationsAPI
val apiServer = embeddedServer(Netty, port = 8080, module = Application::mainModule)

@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation)
    install(Locations)
    install(DefaultHeaders) {
        header("Server", "OSM-API/1.0")
    }

    install(CallLogging) { level = Level.INFO }
    install(StatusPages)

    routing {
        root()
    }
}

private fun Routing.root() {
}
