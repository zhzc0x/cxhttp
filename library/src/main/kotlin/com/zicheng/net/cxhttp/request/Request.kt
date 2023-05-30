package com.zicheng.net.cxhttp.request

import java.io.File


abstract class Request(@get:JvmName("url") val url: String) {

    private var _tag: Any? = null
    private var _headers: MutableMap<String, String>? = null
    private var _params: MutableMap<String, Any>? = null

    @get:JvmName("tag")
    val tag: Any?
        get() = _tag
    @get:JvmName("headers")
    val headers: Map<String, String>?
        get() = _headers
    @get:JvmName("params")
    val params: Map<String, Any>?
        get() = _params

    fun header(name: String, value: String) {
        if (_headers == null) {
            _headers = HashMap()
        }
        _headers!![name] = value
    }

    fun param(key: String, value: Any) {
        if (_params == null) {
            _params = HashMap()
        }
        _params!![key] = value
    }

    fun tag(tag: Any?) {
        _tag = tag
    }

    internal fun apply(headerMap: Map<String, String>?, paramMap: Map<String, Any>? = null): Request {
        headerMap?.forEach{ header(it.key, it.value) }
        paramMap?.forEach{ param(it.key, it.value) }
        return this
    }

    fun containsHeader(key: String): Boolean {
        return _headers?.containsKey(key) ?: false
    }

    fun containsParam(key: String): Boolean {
        return _params?.containsKey(key) ?: false
    }

    /** 预留方法，子类实现 */
    open fun file(paramKey: String, pathList: List<String>) {
        throw IllegalArgumentException("当前Request不支持此参数类型！")
    }

    open fun file(paramKey: String, pathMap: Map<String, String>) {
        throw IllegalArgumentException("当前Request不支持此参数类型！")
    }

    open fun file(file: File, type: String) {
        throw IllegalArgumentException("当前Request不支持此参数类型！")
    }

    internal abstract fun toOkHttp3Request(): okhttp3.Request

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
}