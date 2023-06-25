package com.zicheng.net.cxhttp.request

import java.io.File

sealed interface PartData{
    val name: String
    val value: String?
}


data class StringPart internal constructor(override val name: String, override val value: String): PartData

data class FilePart internal constructor(override val name: String, override val value: String?, val data: File, val contentType: String?): PartData

data class ByteArrayPart internal constructor(override val name: String, override val value: String?, val data: ByteArray, val contentType: String?): PartData