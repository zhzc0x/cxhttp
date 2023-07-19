package com.zicheng.net.cxhttp.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.response.CxHttpResult
import com.zicheng.net.cxhttp.response.Response
import java.lang.reflect.Type

class GsonConverter(private var _gson: Gson? = null,
                                              onConfiguration: GsonBuilder.() -> Unit = {}): CxHttpConverter {

    override val contentType: String = CxHttpHelper.CONTENT_TYPE_JSON
    private val gson: Gson
        get() = _gson!!

    init {
        if(_gson == null){
            val builder = GsonBuilder().apply {
                onConfiguration()
            }
            _gson = builder.create()
        }
    }

    override fun <T> convert(body: Response.Body, tType: Class<T>): T {
        return gson.fromJson(body.string(), tType)
    }

    override fun <T, RESULT : CxHttpResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, tType)
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T, RESULT : CxHttpResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, ParameterizedTypeImpl(List::class.java, tType))
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T> convert(value: T, tType: Class<out T>): ByteArray {
        return gson.toJson(value).toByteArray()
    }

}