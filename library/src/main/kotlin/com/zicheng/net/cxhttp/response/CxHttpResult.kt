package com.zicheng.net.cxhttp.response

import com.zicheng.net.cxhttp.CxHttpHelper

/**
 * CxHttp所有请求返回的结果基类，T为任意类型，默认实现 @see HttpResult
 * 调用者可实现自己的基类，属性名称无限制，但构造器（参数顺序及个数）必须包含与CxHttpResult一致的构造器
 * 例：data class MyHttpResult<T>(val code: Int/String,
 *                            val errorMsg: String,
 *                            val data: T?,): CxHttpResult<T>(code.toString(), errorMsg, data)
 * */
abstract class CxHttpResult<T>(private val cxCode: String,
                               private val cxMsg: String,
                               private val cxData: T?) {

    val success: Boolean = cxCode == CxHttpHelper.SUCCESS_CODE
}

data class HttpResult<T>(val code: String,
                         val msg: String,
                         val data: T?): CxHttpResult<T>(code, msg, data)

@OptIn(CxHttpHelper.InternalAPI::class)
inline fun <reified T, reified RESULT: CxHttpResult<T>> Response.result(): RESULT{
    return if(isSuccessful && body != null){
        try {
            converter.convertResult(body, RESULT::class.java, T::class.java)
        } catch (ex: Exception) {
            CxHttpHelper.exToMessage(ex)
            converter.convertResult(CxHttpHelper.FAILURE_CODE.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        converter.convertResult(code.toString(), message, resultType=RESULT::class.java)
    }
}

@OptIn(CxHttpHelper.InternalAPI::class)
inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> Response.resultList(): RESULT{
    return if(isSuccessful && body != null){
        try {
            converter.convertResultList(body, RESULT::class.java, T::class.java)
        } catch (ex: Exception) {
            CxHttpHelper.exToMessage(ex)
            converter.convertResult(CxHttpHelper.FAILURE_CODE.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        converter.convertResult(code.toString(), message, resultType=RESULT::class.java)
    }
}