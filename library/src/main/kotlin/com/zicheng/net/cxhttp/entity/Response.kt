package com.zicheng.net.cxhttp.entity

import java.io.InputStream

class Response(val code: Int, val message: String, val body: Body?){

    internal val isSuccessful: Boolean
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