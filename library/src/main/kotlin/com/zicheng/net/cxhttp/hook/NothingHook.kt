package com.zicheng.net.cxhttp.hook

import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.request.Request

internal class NothingHook: HookRequest, HookResult {

    override fun invoke(request: Request): Request {
        return request
    }

    override suspend fun <RESULT : CxHttpResult<*>> invoke(result: RESULT): RESULT {
        return result
    }
}