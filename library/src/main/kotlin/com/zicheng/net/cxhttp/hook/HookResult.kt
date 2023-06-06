package com.zicheng.net.cxhttp.hook

import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.request.Request

typealias HookResultFunction = suspend HookResult.(CxHttpResult<*>) -> CxHttpResult<*>

/**
 * 预处理结果：可以根据状态码增加一些操作，比如token失效自动刷新并重试功能
 *
 * */
object HookResult: HookResultFunction {

    val CxHttpResult<*>.code: String
        get() = this.cxCode

    val CxHttpResult<*>.msg: String
        get() = this.cxMsg

    val CxHttpResult<*>.data: Any?
        get() = this.cxData

    val CxHttpResult<*>.request: Request
        get() = this.request

    fun CxHttpResult<*>.setReRequest(value: Boolean){
        reRequest = value
    }

    override suspend fun invoke(hook: HookResult, result: CxHttpResult<*>): CxHttpResult<*> {
        return result
    }

}
