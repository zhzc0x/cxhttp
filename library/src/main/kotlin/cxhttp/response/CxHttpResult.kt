package cxhttp.response

import cxhttp.CxHttpHelper
import cxhttp.annotation.InternalAPI

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
    @InternalAPI
    lateinit var response: Response
    val success: Boolean = cxCode == CxHttpHelper.SUCCESS_CODE
}

data class HttpResult<T>(val code: String,
                         val msg: String,
                         val data: T?): CxHttpResult<T>(code, msg, data)
