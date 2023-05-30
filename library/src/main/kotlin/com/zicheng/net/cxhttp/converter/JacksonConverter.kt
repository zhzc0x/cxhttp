package com.zicheng.net.cxhttp.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.entity.JacksonType
import com.zicheng.net.cxhttp.entity.ParameterizedTypeImpl
import com.zicheng.net.cxhttp.entity.Response
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class JacksonConverter(override val resultClass: Class<*>): CxHttpConverter {

    override val contentType: String = CxHttpHelper.CONTENT_TYPE_JSON

    val jsonMapper = JsonMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(KotlinModule.Builder().build())
    }

    override fun <T, RESULT : CxHttpResult<T>> convert(response: Response, tType: Type): RESULT {
        return if(tType == String::class.java){
            convert(response.code.toString(), response.message, response.body!!.string())
        } else {
            val realType: Type = ParameterizedTypeImpl(resultClass, tType)
            jsonMapper.readValue(response.body!!.string(), JacksonType(realType))
        }
    }

    override fun <T, RESULT : CxHttpResult<List<T>>> convert(response: Response, listType: ParameterizedType): RESULT {
        val realType: Type = ParameterizedTypeImpl(resultClass, listType)
        return jsonMapper.readValue(response.body!!.string(), JacksonType(realType))
    }

    override fun <T> convert(value: T): ByteArray {
        return jsonMapper.writeValueAsBytes(value)
    }

}