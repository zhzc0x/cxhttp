package com.zicheng.net.cxhttp.request

import com.zicheng.net.cxhttp.CxHttpHelper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.io.IOException

fun Request.buildOkhttp3Request(): okhttp3.Request {
    val builder = okhttp3.Request.Builder().url(url)
    if(mergeParamsToUrl){
        builder.url(CxHttpHelper.mergeParamsToUrl(url, params))
    } else {
        builder.url(url)
    }
    headers?.forEach { builder.addHeader(it.key, it.value) }
    var okhttpReqBody = when(body){
        is StringBody -> {
            (body as StringBody).content.toRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is FileBody -> {
            (body as FileBody).content.asRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is ByteArrayBody -> {
            (body as ByteArrayBody).content.toRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is EntityBody<*> -> {
            val entityBody = (body as EntityBody<*>)
            bodyConverter.convert(entityBody.content, entityBody.tType).toRequestBody(bodyConverter.contentType.toMediaTypeOrNull())
        }
        is FormBody -> {
            val formBody = body as FormBody
            params?.forEach { formBody.content.add(StringPart(it.key, it.value.toString())) }
            val bodyBuilder = okhttp3.FormBody.Builder()
            if(formBody.encoded){
                formBody.content.forEach{ bodyBuilder.add(it.name, it.value!!) }
            } else {
                formBody.content.forEach{ bodyBuilder.addEncoded(it.name, it.value!!) }
            }
            bodyBuilder.build()
        }
        is MultipartBody -> {
            val multipartBody = body as MultipartBody
            params?.forEach { multipartBody.content.add(StringPart(it.key, it.value.toString())) }
            val bodyBuilder = okhttp3.MultipartBody.Builder().setType(body!!.contentType.toMediaType())
            for(part in multipartBody.content){
                when(part){
                    is StringPart -> {
                        bodyBuilder.addFormDataPart(part.name, part.value)
                    }
                    is FilePart -> {
                        if (!part.data.exists() || !part.data.isFile) continue
                        val requestBody = if(part.contentType != null){
                            part.data.asRequestBody(part.contentType.toMediaTypeOrNull())
                        } else {
                            part.data.asRequestBody(CxHttpHelper.getMediaType(part.data.name))
                        }
                        bodyBuilder.addFormDataPart(part.name, part.value, requestBody)
                    }
                    is ByteArrayPart -> {
                        bodyBuilder.addFormDataPart(part.name, part.value, part.data.toRequestBody(part.contentType?.toMediaTypeOrNull()))
                    }
                }
            }
            bodyBuilder.build()
        }
        else -> {
            if(!mergeParamsToUrl && params != null){
                bodyConverter.convert(params, Map::class.java).toRequestBody(bodyConverter.contentType.toMediaTypeOrNull())
            } else {
                null
            }
        }
    }
    if (okhttpReqBody != null && onProgress != null) {
        okhttpReqBody = ProgressOkHttp3RequestBody(okhttpReqBody, onProgress!!)
    }
    return builder.method(method, okhttpReqBody).tag(tag).build()
}

internal class ProgressOkHttp3RequestBody(private val requestBody: RequestBody, private val onProgress: (Long, Long) -> Unit): RequestBody() {

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun contentType(): MediaType {
        return requestBody.contentType()!!
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        var currentLength = 0L
        val forwardingSink: ForwardingSink = object : ForwardingSink(sink) {
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                currentLength += byteCount
                onProgress(totalLength, currentLength)
                super.write(source, byteCount)
            }
        }
        val buffer: BufferedSink = forwardingSink.buffer()
        requestBody.writeTo(buffer)
        buffer.flush()
    }

}