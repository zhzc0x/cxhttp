package cxhttp.hook

import cxhttp.CxHttpHelper
import cxhttp.request.Request
import cxhttp.response.Response

/**
 * 预处理结果：可以根据状态码增加一些操作，比如token失效自动刷新并重试功能
 *
 * */
interface HookResponse {

    @OptIn(CxHttpHelper.InternalAPI::class)
    val Response.request: Request
        get() = client.request

    @OptIn(CxHttpHelper.InternalAPI::class)
    fun Response.setReCall() {
        this.reCall = true
    }

}
