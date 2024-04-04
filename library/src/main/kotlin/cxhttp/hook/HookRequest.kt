package cxhttp.hook

import cxhttp.request.Request

/**
 * Hook 请求参数：可以添加一些公共的头信息、参数信息
 *
 * */
fun interface HookRequest {

    suspend fun hook(request: Request)

}