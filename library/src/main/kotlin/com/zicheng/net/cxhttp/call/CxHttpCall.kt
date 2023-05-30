package com.zicheng.net.cxhttp.call

import com.zicheng.net.cxhttp.entity.Response
import com.zicheng.net.cxhttp.exception.CxHttpException
import com.zicheng.net.cxhttp.request.Request


interface CxHttpCall {

//    fun Request.toOkHttp3Request() = this.toOkHttp3Request()

    @Throws(CxHttpException::class)
    suspend fun await(request: Request): Response

}