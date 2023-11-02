package cxhttp.request

import cxhttp.CxHttpHelper
import cxhttp.converter.RequestBodyConverter
import java.io.File
import java.lang.IllegalArgumentException

class Request internal constructor(val url: String, val method: String) {

    private var _tag: Any? = null
    private var _headers: MutableMap<String, String>? = null
    private var _params: MutableMap<String, Any>? = null
    private var _body: Body<*>? = null
        set(value) {
            if (field != null) {
                throw IllegalArgumentException("The body cannot be set repeatedly!")
            }
            field = value
        }
    val tag: Any?
        get() = _tag
    val headers: Map<String, String>?
        get() = _headers
    val params: Map<String, Any>?
        get() = _params
    val body: Body<*>?
        get() = _body

    var mergeParamsToUrl = method == Method.GET.value
    var bodyConverter: RequestBodyConverter = CxHttpHelper.converter
    /** 上传进度监听 (totalLength, currentLength) -> Unit */
    var onProgress: ((Long, Long) -> Unit)? = null

    internal var reCall = false

    fun tag(tag: Any?) {
        _tag = tag
    }

    fun header(name: String, value: String) {
        if (_headers == null) {
            _headers = HashMap()
        }
        _headers!![name] = value
    }

    fun headers(headers: Map<String, String>) {
        if (_headers == null) {
            _headers = HashMap()
        }
        _headers!!.putAll(headers)
    }

    fun param(key: String, value: Any) {
        if (_params == null) {
            _params = HashMap()
        }
        _params!![key] = value
    }

    fun params(params: Map<String, Any>) {
        if (_params == null) {
            _params = HashMap()
        }
        _params!!.putAll(params)
    }

    fun setBody(body: String, contentType: String = CxHttpHelper.CONTENT_TYPE_JSON){
        _body = StringBody(body, contentType)
    }

    fun <T> setBody(body: T, tType: Class<T>, contentType: String = CxHttpHelper.CONTENT_TYPE_JSON,
                    bodyConverter: RequestBodyConverter? = null){
        _body = EntityBody(body, tType, contentType)
        bodyConverter?.let { this.bodyConverter = it }
    }

    fun setBody(body: File, contentType: String = CxHttpHelper.CONTENT_TYPE_OCTET_STREAM){
        _body = FileBody(body, contentType)
    }

    fun setBody(body: ByteArray, contentType: String = CxHttpHelper.CONTENT_TYPE_OCTET_STREAM){
        _body = ByteArrayBody(body, contentType)
    }

    fun formBody(block: FormBody.() -> Unit = {}){
        _body = FormBody(mutableListOf(), CxHttpHelper.CONTENT_TYPE_FORM).apply(block)
    }

    fun multipartBody(type: String = CxHttpHelper.CONTENT_TYPE_MULTIPART_FORM, block: MultipartBody.() -> Unit){
        _body = MultipartBody(mutableListOf(), type).apply(block)
    }

    fun containsHeader(key: String): Boolean {
        return _headers?.containsKey(key) ?: false
    }

    fun containsParam(key: String): Boolean {
        return _params?.containsKey(key) ?: false
    }

    override fun toString(): String {
        val strBuilder = StringBuilder()
        strBuilder.append("url='").append(url).append('\'')
        if (_tag != null) {
            strBuilder.append(", tags=").append(_tag)
        }
        if (_headers != null) {
            strBuilder.append(", headers=").append(_headers)
        }
        if (_params != null) {
            strBuilder.append(", params=").append(_params)
        }
        return strBuilder.toString()
    }

    internal enum class Method(val value: String) {
        GET("GET"), HEAD("HEAD"),
        POST("POST"), DELETE("DELETE"), PUT("PUT"), PATCH("PATCH");
    }
}