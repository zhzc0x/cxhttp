package com.zicheng.net.cxhttp.response

data class HttpResult<T>(val code: String,
                         val msg: String,
                         val data: T?): CxHttpResult<T>(code, msg, data)