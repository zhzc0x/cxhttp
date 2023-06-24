package com.zicheng.net.cxhttp.hook

import com.zicheng.net.cxhttp.response.Response
import com.zicheng.net.cxhttp.request.Request

typealias HookResponseFunction = suspend HookResponse.(Response) -> Response

/**
 * 预处理结果：可以根据状态码增加一些操作，比如token失效自动刷新并重试功能
 *
 * */
class HookResponse internal constructor(): HookResponseFunction {

    val Response.request: Request
        get() = this.request

    fun Response.setReRequest(value: Boolean){
        reRequest = value
    }

    override suspend fun invoke(hook: HookResponse, response: Response): Response {
        return response
    }

}
