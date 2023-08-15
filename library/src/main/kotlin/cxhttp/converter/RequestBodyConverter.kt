package cxhttp.converter

interface RequestBodyConverter {

    val contentType: String

    fun <T> convert(value: T, tType: Class<out T>): ByteArray

}