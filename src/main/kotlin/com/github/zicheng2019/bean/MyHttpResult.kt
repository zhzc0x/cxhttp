package com.github.zicheng2019.bean

import com.zicheng.net.cxhttp.entity.CxHttpResult

data class MyHttpResult<T>(val code: Int,
                           val errorMsg: String,
                           val data: T?): CxHttpResult<T>(code.toString(), errorMsg, data)