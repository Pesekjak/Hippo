package me.pesekjak.hippo

import java.net.URI

/**
 * Utility class for providing checkstyle configuration across modules.
 */
object CheckStyleProvider {

    /**
     * @return URI for the checkstyle configuration
     */
    fun get(): URI {
        return CheckStyleProvider::class.java.getResource("/code_style.xml")!!.toURI()
    }

}