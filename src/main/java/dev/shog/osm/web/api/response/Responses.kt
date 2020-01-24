package dev.shog.osm.web.api.response

open class Response(val response: String? = "OK", val data: Any? = null)

val INVALID_ARGUMENTS = Response("Invalid arguments.")