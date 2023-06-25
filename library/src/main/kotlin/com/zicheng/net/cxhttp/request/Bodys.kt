package com.zicheng.net.cxhttp.request

import java.io.File

sealed interface Body<out T>{
    val content: T
    val contentType: String
}

class StringBody internal constructor(override val content: String, override val contentType: String): Body<String>

class FileBody internal constructor(override val content: File, override val contentType: String): Body<File>

class ByteArrayBody internal constructor(override val content: ByteArray, override val contentType: String): Body<ByteArray>

class EntityBody<T> internal constructor(override val content: T, val tType: Class<T>, override val contentType: String): Body<T>

class FormBody internal constructor(override val content: MutableList<PartData>, override val contentType: String): Body<List<PartData>>{

    var encoded = false

    fun append(name: String, value: String) {
        content += StringPart(name, value)
    }

    fun appends(map: Map<String, String>) {
        for((name, value) in map){
            content += StringPart(name, value)
        }
    }

}

class MultipartBody internal constructor (override val content: MutableList<PartData>, override val contentType: String): Body<List<PartData>>{

    fun append(name: String, value: String) {
        content += StringPart(name, value)
    }

    fun appends(map: Map<String, String>) {
        for((name, value) in map){
            content += StringPart(name, value)
        }
    }

    fun append(name: String, filename: String? = null, filepath: String, contentType: String? = null) {
        content.add(FilePart(name, filename, File(filepath), contentType))
    }

    fun append(name: String, filename: String? = null, file: File, contentType: String? = null) {
        content.add(FilePart(name, filename, file, contentType))
    }

    fun append(name: String, filename: String? = null, data: ByteArray, contentType: String? = null) {
        content.add(ByteArrayPart(name, filename, data, contentType))
    }

}


