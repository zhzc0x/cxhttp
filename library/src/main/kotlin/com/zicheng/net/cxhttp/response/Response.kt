package com.zicheng.net.cxhttp.response

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.request.Request
import java.io.InputStream

class Response(val code: Int, val message: String, val body: Body?){

    @CxHttpHelper.InternalAPI
    lateinit var converter: ResponseConverter
    internal lateinit var request: Request
    internal var reRequest: Boolean = false//是否重新请求

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

@OptIn(CxHttpHelper.InternalAPI::class)
inline fun <reified T> Response.body(): T{
    return body(T::class.java)
}

@OptIn(CxHttpHelper.InternalAPI::class)
fun <T> Response.body(type: Class<T>): T{
    if(type == String::class.java || type == Int::class.java || type == Boolean::class.java || type == Double::class.java || type == Float::class.java){
        @Suppress("UNCHECKED_CAST")
        return body!!.string() as T
    }
    return converter.convert(body!!, type)
}