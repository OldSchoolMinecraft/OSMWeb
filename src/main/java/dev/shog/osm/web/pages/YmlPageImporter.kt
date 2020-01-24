package dev.shog.osm.web.pages

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import dev.shog.osm.web.LOGGER
import dev.shog.lib.FileHandler
import dev.shog.lib.util.toFile
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object YmlPageImporter {
    suspend fun registerPages(routing: Routing): Unit = coroutineScope {
        val yml = ObjectMapper(YAMLFactory()).readTree(dev.shog.osm.web.FileHandler.pagesYml)

        if (yml != null && yml.isArray) {
            JSONArray(yml.toString()).forEach { page ->
                val pg = JSONObject(page.toString())

                when {
                    pg.keys().hasNext() && pg.keys().next() == "0" -> {
                        val obj = pg.getJSONObject("0")

                        if (obj.has("url") && obj.has("file") && obj.has("status")) {
                            val file = "${FileHandler.getApplicationFolder("osmWeb")}${File.separator}${obj.getString("file")}".toFile()

                            if (file.exists()) {
                                routing.get(obj.getString("url")) {
                                    call.respondText(
                                            ContentType.Text.Html,
                                            HttpStatusCode.fromValue(obj.getInt("status"))
                                    ) { String(file.readBytes()) }
                                }
                            } else LOGGER.error("HTML file does not exist!")
                        } else LOGGER.error("Invalid page in page.yml!")
                    }

                    pg.keys().hasNext() && pg.keys().next() == "1" -> {
                        val obj = pg.getJSONObject("1")

                        if (obj.has("url") && obj.has("redirect") && obj.has("permanent")) {
                            routing.get(obj.getString("url")) {
                                call.respondRedirect(obj.getString("redirect"), obj.getBoolean("permanent"))
                            }
                        } else LOGGER.error("Invalid page in page.yml!")
                    }
                }
            }
        }
    }
}