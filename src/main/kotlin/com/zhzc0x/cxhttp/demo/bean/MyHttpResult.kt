package com.zhzc0x.cxhttp.demo.bean

import cxhttp.response.CxHttpResult

data class MyHttpResult<T>(val code: Int,
                           val errorMsg: String,
                           val data: T?): CxHttpResult<T>(code.toString(), errorMsg, data)