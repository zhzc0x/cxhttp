package cxhttp

import cxhttp.annotation.InternalAPI
import cxhttp.call.CxHttpCall
import cxhttp.call.Okhttp3Call
import cxhttp.converter.CxHttpConverter
import cxhttp.converter.JacksonConverter
import cxhttp.hook.*
import cxhttp.response.Response
import cxhttp.request.Request
import cxhttp.response.CxHttpResult
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.URLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

object CxHttpHelper {

    const val HEADER_KEY_ACCEPT = "Accept"
    const val HEADER_KEY_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_TEXT = "text/*"
    const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    const val CONTENT_TYPE_OCTET_STREAM = "application/octet-stream"
    const val CONTENT_TYPE_ZIP = "application/zip"
    const val CONTENT_TYPE_IMAGE_JPEG = "image/jpeg"
    const val CONTENT_TYPE_IMAGE_PNG = "image/png"
    const val CONTENT_TYPE_IMAGE_GIF = "image/gif"

    const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
    const val CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data"

    var debugLog = false
    var SUCCESS_CODE = "0000"

    const val FAIL_CODE_UNKNOWN_HOST = -1001
    const val FAIL_CODE_SSL_ERROR = -1002
    const val FAIL_CODE_NET_TIMEOUT = -1003
    const val FAIL_CODE_CONNECT_ERROR = -1004
    const val FAIL_CODE_UNKNOWN_ERROR = -1005
    const val FAIL_CODE_PARSER_ERROR = -1006

    internal lateinit var scope: CoroutineScope
    internal lateinit var call: CxHttpCall
    internal lateinit var converter: CxHttpConverter
    internal var hookRequest: HookRequest = HookInstance
    internal var hookResponse: suspend HookResponse.(Response) -> Response = {
        it
    }
    internal var hookResult: suspend HookResult.(CxHttpResult<*>) -> CxHttpResult<*> = {
        it
    }

    @JvmOverloads
    fun init(scope: CoroutineScope, debugLog: Boolean, call: CxHttpCall = Okhttp3Call {
        callTimeout(15, TimeUnit.SECONDS)
        if (debugLog) {
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
    }, converter: CxHttpConverter = JacksonConverter()
    ){
        this.scope = scope
        this.debugLog = debugLog
        this.call = call
        this.converter = converter
    }

    fun hookRequest(hookRequest: HookRequest) {
        this.hookRequest = hookRequest
    }

    fun hookResponse(hookResponse: suspend HookResponse.(Response) -> Response) {
        this.hookResponse = hookResponse
    }

    fun hookResult(hookResult: suspend HookResult.(CxHttpResult<*>) -> CxHttpResult<*>) {
        this.hookResult = hookResult
    }

    internal suspend inline fun applyHookRequest(request: Request) {
        hookRequest.hook(request)
    }

    internal suspend inline fun applyHookResponse(response: Response): Response {
        return HookInstance.hookResponse(response)
    }

    @InternalAPI
    suspend fun applyHookResult(result: CxHttpResult<*>): CxHttpResult<*> {
        return HookInstance.hookResult(result)
    }

    internal fun getMediaType(fName: String): MediaType {
        var contentType: String? = URLConnection.guessContentTypeFromName(fName)
        if (contentType.isNullOrEmpty()) {
            contentType = CONTENT_TYPE_OCTET_STREAM
        }
        return contentType.toMediaType()
    }

    internal fun mergeParamsToUrl(url: String, params: Map<String, Any>?): String {
        return if (params != null) {
            val appendUrl = StringBuilder(url)
            val iterator = params.entries.iterator()
            appendUrl.append("?")
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                appendUrl.append(key).append("=").append(value)
                if (!iterator.hasNext()) {
                    break
                }
                appendUrl.append("&")
            }
            appendUrl.toString()
        } else {
            url
        }
    }

    @InternalAPI
    fun exToFailInfo(ex: Exception): FailInfo {
        if (debugLog) {
            ex.printStackTrace()
        }
        val failInfo = when (ex) {
            is UnknownHostException -> {
                FailInfo(FAIL_CODE_UNKNOWN_HOST, "域名解析异常")
            }
            is SSLException -> {
                FailInfo(FAIL_CODE_SSL_ERROR, "SSL异常")
            }
            is InterruptedIOException -> {
                FailInfo(FAIL_CODE_NET_TIMEOUT, "网络超时")
            }
            is SocketException -> {
                FailInfo(FAIL_CODE_CONNECT_ERROR, "连接异常")
            }
            else -> {
                FailInfo(FAIL_CODE_UNKNOWN_ERROR,  "未知异常")
            }
        }
        if (ex.message != null) {
            failInfo.msg = "${failInfo.msg}：${ex.message}"
        }
        return failInfo
    }

    @InternalAPI
    data class FailInfo(val code: Int, var msg: String)

}
