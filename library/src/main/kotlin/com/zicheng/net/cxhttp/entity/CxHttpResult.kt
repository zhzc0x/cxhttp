package com.zicheng.net.cxhttp.entity

import com.zicheng.net.cxhttp.CxHttpHelper
import com.zicheng.net.cxhttp.request.Request

/**
 * 基础数据抽象类，T为任意类型
 * 调用者根据自己的需求定制基础数据类，自定义属性名称和code属性类型，构造器需要包含构造参数顺序及个数与CxHttpResult一致的构造器，其它无限制，例：
 * data class MyHttpResult<T>(val code: Int,
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