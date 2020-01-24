package dev.shog.osm.web

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level

@KtorExperimentalLocationsAPI
val cdnServer = embeddedServer(Netty, port = 8000, module = Application::mainModule)

@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation)
    install(Locations)
    install(DefaultHeaders) {
        header("Server", "OSM-CDN/1.0")
    }

    install(CachingHeaders) {
        options { outgoingContent ->
            val def = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> def
                ContentType.Text.JavaScript -> def
                ContentType.Text.Html -> def
                ContentType.Text.Plain -> def
                else -> null
            }
        }
    }

    install(CallLogging) { level = Level.INFO }

    install(StatusPages)

    routing {
        static {
            files(FileHandler.cdn)
        }
    }
}
