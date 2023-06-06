package com.zicheng.net.cxhttp.entity

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.request.Request

/**
 * CxHttp所有请求返回的结果基类，T为任意类型，默认实现 @see HttpResult
 * 调用者可实现自己的基类，属性名称无限制，但构造器（参数顺序及个数）必须包含与CxHttpResult一致的构造器
 * 例：data class MyHttpResult<T>(val code: Int/String,
 *                            val errorMsg: String,
 *                            val data: T?,): CxHttpResult<T>(code.toString(), errorMsg, data)
 * */
abstract class CxHttpResult<T>(internal val cxCode: String,
                               internal val cxMsg: String,
                               internal val cxData: T?) {

    internal lateinit var request: Request
    internal var reRequest: Boolean = false//是否重新请求

    val success: Boolean = cxCode == CxHttpHelper.SUCCESS_CODE
}