package dev.shog.osm.web.api

import dev.shog.osm.web.CONFIG
import dev.shog.osm.web.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

/**
 * Manage MySQL conecitons
 */
object MySql {
    private val creds = CONFIG.asObject<Config>().mysql ?: exitProcess(2)

    /**
     * Create a connection to the MySQL.
     */
    suspend fun createConnection(db: String): Connection = coroutineScope {
        Class.forName("com.mysql.cj.jdbc.Driver")
        withContext(Dispatchers.Unconfined) {
            DriverManager.getConnection(creds.url + "/$db", creds.username, creds.password)
        }
    }
}