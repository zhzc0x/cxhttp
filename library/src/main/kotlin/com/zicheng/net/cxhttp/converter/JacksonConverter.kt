package com.zicheng.net.cxhttp.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonWriteFeature
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

class JacksonConverter @JvmOverloads constructor(override val resultClass: Class<*>, private var _jsonMapper: JsonMapper? = null,
                                                 onConfiguration: JsonMapper.Builder.() -> Unit = {}): CxHttpConverter {

    override val contentType: String = CxHttpHelper.CONTENT_TYPE_JSON
    private val jsonMapper: JsonMapper
        get() = _jsonMapper!!

    init {
        if(_jsonMapper == null){
            val builder = JsonMapper.builder().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS, true)
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                defaultDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()))
                serializationInclusion(JsonInclude.Include.NON_NULL)
                onConfiguration()
                addModule(KotlinModule.Builder().build())
            }
            _jsonMapper = builder.build()
        }
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