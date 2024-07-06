package cxhttp.response

import cxhttp.CxHttp
import cxhttp.CxHttpHelper
import cxhttp.annotation.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.Reader

data class Response(val code: Int, val message: String, val body: Body?) {

    @InternalAPI
    lateinit var client: CxHttp
    @InternalAPI
    var reCall: Boolean = false//是否重新请求
        internal set(value) {
            field = value
            client.request.reCall = field
        }

    val isSuccessful: Boolean
        get() = code in 200..299

    abstract class Body {

        abstract fun string(): String

        open fun bytes(): ByteArray {
            throw IllegalArgumentException("当前Body不支持此数据类型！")
        }

        open fun charStream(): Reader {
            throw IllegalArgumentException("当前Body不支持此数据类型！")
        }

        open fun byteStream(): InputStream {
            throw IllegalArgumentException("当前Body不支持此数据类型！")
        }
    }
}

suspend inline fun <reified T> Response.body(): T {
    return bodyOrNull(T::class.java)!!
}

suspend fun <T> Response.body(type: Class<T>): T {
    return bodyOrNull(type)!!
}

suspend inline fun <reified T> Response.bodyOrNull(): T? {
    return bodyOrNull(T::class.java)
}

@OptIn(InternalAPI::class)
suspend fun <T> Response.bodyOrNull(type: Class<T>): T? = withContext(Dispatchers.IO) {
    if (body == null) {
        return@withContext null
    }
    try {
        if (isBasicType(type)) {
            @Suppress("UNCHECKED_CAST")
            body.string() as T
        } else {
            client.respConverter.convert(body, type)
        }
    } catch (ex: Exception) {
        if (CxHttpHelper.debugLog) {
            ex.printStackTrace()
        }
        null
    }
}

@OptIn(InternalAPI::class)
suspend inline fun <reified T, reified RESULT: CxHttpResult<T>> Response.result(): RESULT = withContext(Dispatchers.IO) {
    var result = convertResult<T, RESULT>(this@result)
    result.response = this@result
    result = CxHttpHelper.applyHookResult(result) as RESULT
    if (result.response.reCall) {
        convertResult<T, RESULT>(client.awaitImpl())
    } else {
        result
    }
}

@InternalAPI
inline fun <reified T, reified RESULT: CxHttpResult<T>> convertResult(response: Response): RESULT {
    return if (response.isSuccessful && response.body != null) {
        try {
            if (isBasicType(T::class.java)) {
                response.client.respConverter.convertResult(response.code.toString(), response.message, response.body.string() as T, RESULT::class.java)
            } else {
                response.client.respConverter.convertResult(response.body, RESULT::class.java, T::class.java)
            }
        } catch (ex: Exception) {
            if (CxHttpHelper.debugLog) {
                ex.printStackTrace()
            }
            response.client.respConverter.convertResult(CxHttpHelper.FAIL_CODE_PARSER_ERROR.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        response.client.respConverter.convertResult(response.code.toString(), response.message, resultType=RESULT::class.java)
    }
}

@OptIn(InternalAPI::class)
suspend inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> Response.resultList(): RESULT = withContext(Dispatchers.IO) {
    var result = convertResultList<T, RESULT>(this@resultList)
    result.response = this@resultList
    result = CxHttpHelper.applyHookResult(result) as RESULT
    if (result.response.reCall) {
        convertResultList<T, RESULT>(client.awaitImpl())
    } else {
        result
    }
}

@InternalAPI
inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> convertResultList(response: Response): RESULT {
    return if (response.isSuccessful && response.body != null) {
        try {
            response.client.respConverter.convertResultList(response.body, RESULT::class.java, T::class.java)
        } catch (ex: Exception) {
            if (CxHttpHelper.debugLog) {
                ex.printStackTrace()
            }
            response.client.respConverter.convertResult(CxHttpHelper.FAIL_CODE_PARSER_ERROR.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        response.client.respConverter.convertResult(response.code.toString(), response.message, resultType=RESULT::class.java)
    }
}

@InternalAPI
fun <T> isBasicType(type: Class<T>): Boolean {
    return type == String::class.java || type == Int::class.java || type == Long::class.java ||
            type == Boolean::class.java || type == Double::class.java || type == Float::class.java
}
