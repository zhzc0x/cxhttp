package com.zicheng.net.cxhttp.request

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


internal class PostFileRequest(url: String): Request(url) {

    private lateinit var file: File
    private lateinit var type: String

    override fun file(file: File, type: String) {
        this.file = file
        this.type = type
    }

    override fun toOkHttp3Request(): okhttp3.Request {
        val builder = okhttp3.Request.Builder().url(url)
        headers?.forEach { builder.addHeader(it.key, it.value) }
        val requestBody = file.asRequestBody(type.toMediaTypeOrNull())
        return builder.tag(tag).post(requestBody).build()
    }

}