package dev.shog.osm.web

import dev.shog.lib.FileHandler
import dev.shog.lib.util.toFile
import java.io.File

object FileHandler {
    var cdn: File
    var pagesYml: File

    init {
        val home = FileHandler.getApplicationFolder("osmWeb")

        cdn = "${home}${File.separator}cdn".toFile()
        pagesYml = "${home}${File.separator}pages.yml".toFile()

        if (!pagesYml.exists())
            pagesYml.createNewFile()

        if (!cdn.exists())
            cdn.mkdirs()
    }
}