package dev.shog.osm.web.data

/**
 * The config class.
 */
data class Config(
        val webhook: String? = "",
        val mysql: MySql? = MySql("", "")
)

data class MySql(val username: String? = "", val password: String? = "")