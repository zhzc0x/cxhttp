package com.zicheng.net.cxhttp.converter

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.entity.Response
import java.lang.reflect.Type

interface ResponseConverter {

    val resultClass: Class<*>

    fun <T> convert(body: Response.Body, type: Class<T>): T

    fun <T, RESULT: CxHttpResult<T>> convertResult(body: Response.Body, tType: Type): RESULT

    fun <T, RESULT: CxHttpResult<List<T>>> convertResultList(body: Response.Body, tType: Type): RESULT

    @CxHttpHelper.InternalAPI
    fun <RESULT: CxHttpResult<*>> convertResult(code: String, msg: String, data: Any? = null): RESULT{
        val httpResult = try {
            val constructor = resultClass.getConstructor(String::class.java, String::class.java, Any::class.java)
            constructor.newInstance(code, msg, data)
        } catch (_: NoSuchMethodException){
            try {
                val constructor = resultClass.getConstructor(Int::class.java, String::class.java, Any::class.java)
                constructor.newInstance(code.toInt(), msg, data)
            } catch (_: NoSuchMethodException){
                throw IllegalArgumentException("请保证resultClass(RESULT: CxHttpResult<T>)的构造器参数类型及顺序为" +
                        "(String, String, T)或者(Int, String, T), 否则内部无法完成convert")
            }
        }
        @Suppress("UNCHECKED_CAST")
        return httpResult as RESULT
    }

}