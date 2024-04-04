package cxhttp.hook

import cxhttp.CxHttpHelper
import cxhttp.request.Request
import cxhttp.response.CxHttpResult

interface HookResult {


    @OptIn(CxHttpHelper.InternalAPI::class)
    val CxHttpResult<*>.request: Request
        get() = response.client.request

    @OptIn(CxHttpHelper.InternalAPI::class)
    fun CxHttpResult<*>.setReCall() {
        response.reCall = true
    }

}