package dev.shog.osm.web.api

import dev.shog.osm.web.CONFIG
import dev.shog.osm.web.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

object MySql {
    private val creds = CONFIG.asObject<Config>().mysql ?: exitProcess(2)

    /**
     * Create a connection to the AWS.
     */
    suspend fun createConnection(): Connection = coroutineScope {
        Class.forName("com.mysql.jdbc.Driver")

        withContext(Dispatchers.Unconfined) {
            DriverManager.getConnection(
                    "jdbc:mysql://144.217.80.107:3306/accounts?user=${creds.username}&password=${creds.password}"
            )
        }
    }
}