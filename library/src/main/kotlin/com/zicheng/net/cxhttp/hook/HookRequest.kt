package com.zicheng.net.cxhttp.hook

import com.zicheng.net.cxhttp.request.Request

/**
 * Hook 请求参数：可以添加一些公共的头信息、参数信息
 *
 * */
interface HookRequest{

    operator fun invoke(request: Request): Request

}