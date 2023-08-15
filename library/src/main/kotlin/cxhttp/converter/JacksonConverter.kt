package cxhttp.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import cxhttp.CxHttpHelper
import cxhttp.response.CxHttpResult
import cxhttp.response.Response
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class JacksonConverter(private var _jsonMapper: JsonMapper? = null,
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

    override fun <T> convert(body: Response.Body, tType: Class<T>): T {
        return jsonMapper.readValue(body.string(), JacksonType(tType))
    }

    override fun <T, RESULT : CxHttpResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, tType)
        return jsonMapper.readValue(body.string(), JacksonType(realType))
    }

    override fun <T, RESULT : CxHttpResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, ParameterizedTypeImpl(List::class.java, tType))
        return jsonMapper.readValue(body.string(), JacksonType(realType))
    }

    override fun <T> convert(value: T, tClass: Class<out T>): ByteArray {
        return jsonMapper.writeValueAsBytes(value)
    }

}

class JacksonType<T>(private val type: Type) : TypeReference<T>() {
    override fun getType(): Type {
        return type
    }
}