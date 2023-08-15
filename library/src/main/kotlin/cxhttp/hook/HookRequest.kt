package cxhttp.hook

import cxhttp.request.Request

typealias HookRequestFunction = suspend HookRequest.(Request) -> Request

/**
 * Hook 请求参数：可以添加一些公共的头信息、参数信息
 *
 * */
class HookRequest internal constructor(): HookRequestFunction {
    override suspend fun invoke(hook: HookRequest, request: Request): Request {
        return request
    }
}