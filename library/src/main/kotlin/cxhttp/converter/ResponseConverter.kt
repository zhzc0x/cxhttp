package cxhttp.converter

import cxhttp.annotation.InternalAPI
import cxhttp.response.CxHttpResult
import cxhttp.response.Response
import java.lang.reflect.Type

interface ResponseConverter {

    fun <T> convert(body: Response.Body, tType: Class<T>): T

    fun <T, RESULT: CxHttpResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT

    fun <T, RESULT: CxHttpResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT

    @InternalAPI
    fun <RESULT: CxHttpResult<*>> convertResult(code: String, msg: String, data: Any? = null, resultType: Class<RESULT>): RESULT {
        val httpResult = try {
            val constructor = resultType.getConstructor(String::class.java, String::class.java, Any::class.java)
            constructor.isAccessible = true
            constructor.newInstance(code, msg, data)
        } catch (_: NoSuchMethodException) {
            try {
                val constructor = resultType.getConstructor(Int::class.java, String::class.java, Any::class.java)
                constructor.isAccessible = true
                constructor.newInstance(code.toInt(), msg, data)
            } catch (_: NoSuchMethodException) {
                throw IllegalArgumentException("请保证resultClass(RESULT: CxHttpResult<T>)的构造器参数类型及顺序为" +
                        "(String, String, T)或者(Int, String, T), 否则内部无法完成convert")
            }
        }
        return httpResult
    }

}