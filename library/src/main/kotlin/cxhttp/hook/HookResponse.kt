package cxhttp.hook

import cxhttp.annotation.InternalAPI
import cxhttp.request.Request
import cxhttp.response.Response

/**
 * 预处理结果：可以根据状态码增加一些操作，比如token失效自动刷新并重试功能
 *
 * */
interface HookResponse {

    @OptIn(InternalAPI::class)
    val Response.request: Request
        get() = client.request

    @OptIn(InternalAPI::class)
    fun Response.setReCall() {
        this.reCall = true
    }

}
