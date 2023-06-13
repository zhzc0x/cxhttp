package com.zicheng.net.cxhttp.request

import java.io.File

sealed interface PartData{
    val name: String
    val value: String?
}


internal data class StringPart(override val name: String, override val value: String): PartData

internal data class FilePart(override val name: String, override val value: String?, val data: File, val contentType: String?): PartData

internal data class ByteArrayPart(override val name: String, override val value: String?, val data: ByteArray, val contentType: String?): PartData