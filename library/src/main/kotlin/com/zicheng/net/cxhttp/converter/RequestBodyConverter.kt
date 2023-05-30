package com.zicheng.net.cxhttp.converter

interface RequestBodyConverter {

    val contentType: String

    fun <T> convert(value: T): ByteArray

}