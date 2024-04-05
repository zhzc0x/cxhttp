package cxhttp.hook

import cxhttp.request.Request

internal object HookInstance: HookRequest, HookResponse, HookResult {

    override suspend fun hook(request: Request) {

    }

}
