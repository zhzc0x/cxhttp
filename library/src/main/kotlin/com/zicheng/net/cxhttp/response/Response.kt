package com.zicheng.net.cxhttp.response

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.request.Request
import java.io.InputStream

data class Response(val code: Int, val message: String, val body: Body?){

    internal lateinit var request: Request
    internal var reRequest: Boolean = false//是否重新请求
    @CxHttpHelper.InternalAPI
    lateinit var converter: ResponseConverter

    val isSuccessful: Boolean
        get() = code in 200..299

    abstract class Body{

        abstract fun string(): String

        open fun bytes(): ByteArray{
            throw IllegalArgumentException("当前Body不支持此数据类型！")
        }

        open fun byteStream(): InputStream{
            throw IllegalArgumentException("当前Body不支持此数据类型！")
        }

    }

}

inline fun <reified T> Response.body(): T{
    return bodyOrNull(T::class.java)!!
}

fun <T> Response.body(type: Class<T>): T{
    return bodyOrNull(type)!!
}

inline fun <reified T> Response.bodyOrNull(): T?{
    return bodyOrNull(T::class.java)
}

@OptIn(CxHttpHelper.InternalAPI::class)
fun <T> Response.bodyOrNull(type: Class<T>): T?{
    if(body == null){
        return null
    }
    return try {
        if(type == String::class.java || type == Int::class.java || type == Long::class.java ||
            type == Boolean::class.java || type == Double::class.java || type == Float::class.java){
            @Suppress("UNCHECKED_CAST")
            body.string() as T
        } else {
            converter.convert(body, type)
        }
    } catch (ex: Exception){
        if(CxHttpHelper.debugLog){
            ex.printStackTrace()
        }
        null
    }
}