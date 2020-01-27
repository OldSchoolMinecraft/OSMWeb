package dev.shog.osm.web

import dev.shog.lib.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.lib.hook.WebhookUser
import dev.shog.osm.web.api.apiServer
import dev.shog.osm.web.data.Config
import io.ktor.locations.KtorExperimentalLocationsAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

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

var URLS = Urls("http://localhost:8000", "http://localhost:8020", "http://localhost:8080")

/**
 * Main
 */
@KtorExperimentalLocationsAPI
fun main(args: Array<String>) {
    if (args.contains("--prod"))
        URLS = Urls("https://cdn.oldschoolminecraft.com", "https://api.oldschoolminecraft.com", "https://oldschoolminecraft.com")

    if (!args.contains("--block-init-notif"))
        DISCORD.sendMessage("OSMWeb is now online!").subscribe()

    apiServer.start(true)
}

