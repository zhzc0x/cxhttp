package com.zicheng.net.cxhttp.request

import com.zicheng.net.cxhttp.converter.RequestBodyConverter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

internal class PostRequest(url: String, private val reqBodyConverter: RequestBodyConverter, private val paramEntity: Any? = null):
    Request(url) {

    override fun toOkHttp3Request(): okhttp3.Request {
        val builder = okhttp3.Request.Builder().url(url)
        headers?.forEach { builder.addHeader(it.key, it.value) }
        val requestBody = if(paramEntity == null){
            reqBodyConverter.convert(params)
        } else {
            reqBodyConverter.convert(paramEntity)
        }.toRequestBody(reqBodyConverter.contentType.toMediaTypeOrNull())
        return builder.tag(tag).post(requestBody).build()
    }

}