package com.zicheng.net.cxhttp.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.entity.*
import java.lang.reflect.Type

class GsonConverter @JvmOverloads constructor(override val resultClass: Class<*>, private var _gson: Gson? = null,
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

    override fun <T> convert(body: Response.Body, type: Class<T>): T {
        return gson.fromJson(body.string(), TypeToken.get(type))
    }

    override fun <T, RESULT : CxHttpResult<T>> convertResult(body: Response.Body, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultClass, tType)
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T, RESULT : CxHttpResult<List<T>>> convertResultList(body: Response.Body, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultClass, ParameterizedTypeImpl(List::class.java, tType))
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T> convert(value: T, tClass: Class<out T>): ByteArray {
        return gson.toJson(value).toByteArray()
    }

}