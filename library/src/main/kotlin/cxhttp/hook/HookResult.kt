package cxhttp.hook

import cxhttp.annotation.InternalAPI
import cxhttp.request.Request
import cxhttp.response.CxHttpResult

interface HookResult {

    @OptIn(InternalAPI::class)
    val CxHttpResult<*>.request: Request
        get() = response.client.request

    @OptIn(InternalAPI::class)
    fun CxHttpResult<*>.setReCall() {
        response.reCall = true
    }

}
