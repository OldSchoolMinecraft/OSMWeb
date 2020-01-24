package dev.shog.osm.web

import dev.shog.lib.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.lib.hook.WebhookUser
import dev.shog.osm.web.api.apiServer
import dev.shog.osm.web.data.Config
import dev.shog.osm.web.data.Session
import dev.shog.osm.web.pages.YmlPageImporter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.system.exitProcess

@KtorExperimentalLocationsAPI
val mainServer = embeddedServer(Netty, port = 8090, module = Application::mainModule)

@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation)
    install(Locations)
    install(DefaultHeaders) {
        header("Server", "OSM/1.0")
    }

    install(CachingHeaders) {
        options { outgoingContent ->
            val def = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> def
                ContentType.Text.JavaScript -> def
                else -> null
            }
        }
    }

    install(CallLogging) { level = Level.INFO }

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory()) {
            cookie.path = "/"
        }
    }

    install(StatusPages)

    routing {
        runBlocking { YmlPageImporter.registerPages(this@routing) }
    }
}

val LOGGER: Logger = LoggerFactory.getLogger("osmWeb")

/**
 * This creates or gets the config from `%appdata%\shogdev\osmWeb`
 * The name of the config should be `cfg.yml`
 */
val CONFIG = ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "osmWeb", Config(), false)

/**
 * The Discord Webhook.
 */
val DISCORD: DiscordWebhook by lazy {
    val cfg = CONFIG.asObject<Config>()
    val webhookUser = WebhookUser("OSMWeb", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2F1.bp.blogspot.com%2F-ijXZS5yCAFw%2FUbDbYUNO2LI%2FAAAAAAAAAMk%2FuZW9W9WY7cA%2Fs1600%2Fred%2Bbeardie.jpg&f=1&nofb=1")

    if (cfg.webhook.isNullOrBlank()) {
        LOGGER.error("Webhook is unset in Config!")
        exitProcess(1)
    } else DiscordWebhook(cfg.webhook, webhookUser)
}

data class Urls(val cdn: String, val api: String, val main: String)

var URLS = Urls("http://localhost:8010", "http://localhost:8020", "http://localhost:8080")

/**
 * Main
 */
@KtorExperimentalLocationsAPI
fun main(args: Array<String>) {

    if (args.contains("--prod"))
        URLS = Urls("https://cdn.oldschoolminecraft.com", "https://api.oldschoolminecraft.com", "https://oldschoolminecraft.com")

    if (!args.contains("--block-init-notif"))
        DISCORD.sendMessage("OSMWeb is now online!").subscribe()

    cdnServer.start()
    apiServer.start()
    mainServer.start(true)
}

