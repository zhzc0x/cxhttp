package com.zicheng.net.cxhttp.entity

data class HttpResult<T>(val code: String,
                         val msg: String,
                         val data: T?): CxHttpResult<T>(code, msg, data)