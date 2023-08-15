package cxhttp.call

import cxhttp.response.Response
import cxhttp.request.Request


interface CxHttpCall {

    suspend fun await(request: Request): Response

}