package dev.shog.osm.web.data

/**
 * The config class.
 */
data class Config(
        val webhook: String? = "",
        val mysql: MySql? = MySql("", "", "")
)

/**
 * The MySQL credentials
 */
data class MySql(
        val username: String? = "",
        val password: String? = "",
        val url: String? = null
)