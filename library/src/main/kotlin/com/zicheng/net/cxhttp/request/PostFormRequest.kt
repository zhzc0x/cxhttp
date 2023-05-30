package com.zicheng.net.cxhttp.request

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.entity.UploadFileWrapper
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody

internal class PostFormRequest(url: String, private val onProgress: ((Long, Long) -> Unit)? = null): Request(url) {

    private var uploadFileList: MutableList<UploadFileWrapper>? = null

    override fun file(paramKey: String, pathList: List<String>) {
        uploadFileList = ArrayList()
        for (path in pathList) {
            val uploadFile = UploadFileWrapper(paramKey, path)
            uploadFileList!!.add(uploadFile)
        }
    }

    override fun file(paramKey: String, pathMap: Map<String, String>) {
        uploadFileList = ArrayList()
        for ((name, value) in pathMap) {
            val uploadFile = UploadFileWrapper(paramKey, value)
            uploadFile.setValue(name)
            uploadFileList!!.add(uploadFile)
        }
    }

    override fun toOkHttp3Request(): okhttp3.Request {
        val builder = okhttp3.Request.Builder().url(url)
        headers?.forEach { builder.addHeader(it.key, it.value) }
        var requestBody = if (uploadFileList.isNullOrEmpty()) {
            val bodyBuilder = FormBody.Builder()
            params?.forEach{ bodyBuilder.add(it.key, it.value.toString()) }
            bodyBuilder.build()
        } else {
            val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            params?.forEach{ bodyBuilder.addFormDataPart(it.key, it.value.toString()) }
            for (uploadFile in uploadFileList!!) {
                if (!uploadFile.exists() || !uploadFile.isFile) continue
                val requestBody: RequestBody = uploadFile.asRequestBody(CxHttpHelper.getMediaType(uploadFile.name))
                bodyBuilder.addFormDataPart(uploadFile.getKey(), uploadFile.getValue(), requestBody)
            }
            bodyBuilder.build()
        }
        if (onProgress != null) {
            requestBody = MultipartRequestBody(requestBody, onProgress)
        }
        return builder.tag(tag).post(requestBody).build()
    }

    override fun toString(): String {
        return if (uploadFileList != null) {
            "${super.toString()}, uploadFileList=$uploadFileList"
        } else {
            super.toString()
        }
    }

}