package com.zicheng.net.cxhttp

import com.zicheng.net.cxhttp.call.CxHttpCall
import com.zicheng.net.cxhttp.call.OkHttp3Call
import com.zicheng.net.cxhttp.converter.CxHttpConverter
import com.zicheng.net.cxhttp.converter.JacksonConverter
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.entity.HttpResult
import com.zicheng.net.cxhttp.exception.CxHttpException
import com.zicheng.net.cxhttp.hook.*
import com.zicheng.net.cxhttp.request.Request
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.URLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLPeerUnverifiedException


object CxHttpHelper {

    const val HEADER_KEY_ACCEPT = "Accept"
    const val HEADER_KEY_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    const val CONTENT_TYPE_OCTET_STREAM = "application/octet-stream"
    const val CONTENT_TYPE_ZIP = "application/zip"
    const val CONTENT_TYPE_IMAGE_JPEG = "image/jpeg"
    const val CONTENT_TYPE_IMAGE_PNG = "image/png"
    const val CONTENT_TYPE_IMAGE_GIF = "image/gif"
    var SUCCESS_CODE = "0000"
    var FAILURE_CODE = "-1000"

    internal lateinit var scope: CoroutineScope
    internal lateinit var call: CxHttpCall
    internal lateinit var converter: CxHttpConverter
    internal var debugLog = false
    private val hookRequestInstance = HookRequest()
    private val hookResultInstance = HookResult()
    internal var hookRequest: HookRequestFunction = hookRequestInstance
    internal var hookResult: HookResultFunction = hookResultInstance

    @JvmOverloads
    fun init(scope: CoroutineScope, debugLog: Boolean, call: CxHttpCall = OkHttp3Call{
        it.callTimeout(15, TimeUnit.SECONDS)
        if (debugLog) {
            it.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
    }, converter: CxHttpConverter = JacksonConverter(HttpResult::class.java)){
        this.scope = scope
        this.debugLog = debugLog
        this.call = call
        this.converter = converter
    }

    fun hookRequest(hook: HookRequestFunction){
        hookRequest = hook
    }

    fun hookResult(hook: HookResultFunction){
        hookResult = hook
    }

    internal suspend inline fun applyHookRequest(request: Request): Request {
        return hookRequestInstance.hookRequest(request)
    }

    internal suspend inline fun <RESULT: CxHttpResult<*>> applyHookResult(result: CxHttpResult<*>): RESULT{
        @Suppress("UNCHECKED_CAST")
        return hookResultInstance.hookResult(result) as RESULT
    }

    internal fun getMediaType(fName: String): MediaType {
        var contentType: String? = URLConnection.guessContentTypeFromName(fName)
        if (contentType.isNullOrEmpty()) {
            contentType = CONTENT_TYPE_OCTET_STREAM
        }
        return contentType.toMediaType()
    }

    internal fun exToMessage(ex: Exception): String {
        if(debugLog){
            ex.printStackTrace()
        }
        val msg = if(ex is CxHttpException){
            when (ex.ie) {
                is UnknownHostException, is SSLPeerUnverifiedException -> {
                    "域名解析异常"
                }
                is InterruptedIOException, is SocketException -> {
                    "网络异常"
                }
                else -> {
                    "未知异常"
                }
            }
        } else {
            "数据解析异常"
        }
        return if(ex.message != null){
            "$msg：${ex.message}"
        } else {
            msg
        }
    }

}