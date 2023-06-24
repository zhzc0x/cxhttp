package com.zicheng.net.cxhttp.call

import com.zicheng.net.cxhttp.response.Response
import com.zicheng.net.cxhttp.request.Request


interface CxHttpCall {

    suspend fun await(request: Request): Response

}