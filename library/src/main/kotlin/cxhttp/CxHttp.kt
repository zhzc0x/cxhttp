package cxhttp

import cxhttp.annotation.InternalAPI
import cxhttp.converter.ResponseConverter
import cxhttp.response.CxHttpResult
import cxhttp.response.Response
import cxhttp.request.Request
import cxhttp.response.result
import cxhttp.response.resultList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

/**
 * Coroutine Extensions Http（协程扩展Http）
 *
 * */
class CxHttp private constructor(internal val request: Request, private val block: suspend Request.() -> Unit) {

    companion object {

        fun get(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.GET.value, block)
        }

        fun head(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.HEAD.value, block)
        }

        fun post(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.POST.value, block)
        }

        fun delete(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.DELETE.value, block)
        }

        fun put(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.PUT.value, block)
        }

        fun patch(url: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return request(url, Request.Method.PATCH.value, block)
        }

        fun request(url: String, method: String, block: suspend Request.() -> Unit = {}): CxHttp {
            return CxHttp(Request(url, method), block)
        }

        fun request(request: Request): CxHttp {
            return CxHttp(request){}
        }

    }

    @InternalAPI
    var scope: CoroutineScope = CxHttpHelper.scope
        private set
    @InternalAPI
    var respConverter: ResponseConverter = CxHttpHelper.converter
        private set

    @OptIn(InternalAPI::class)
    fun scope(scope: CoroutineScope) = apply {
        this.scope = scope
    }

    /**
     * 设置ResponseConverter，自定义转换Response to CxHttpResult，默认使用CxHttpHelper.converter
     *
     * */
    @OptIn(InternalAPI::class)
    fun respConverter(respConverter: ResponseConverter) = apply {
        this.respConverter = respConverter
    }

    @OptIn(InternalAPI::class)
    inline fun launch(crossinline responseBlock: suspend CoroutineScope.(Response) -> Unit) = scope.launch {
        responseBlock(await())
    }

    @OptIn(InternalAPI::class)
    inline fun <reified T, reified RESULT: CxHttpResult<T>> launchResult(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResult())
    }

    @OptIn(InternalAPI::class)
    inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> launchResultList(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResultList())
    }

    @OptIn(InternalAPI::class)
    suspend fun await(): Response = withContext(Dispatchers.IO) {
        awaitImpl()
    }

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T, reified RESULT: CxHttpResult<T>> awaitResult(): RESULT = withContext(Dispatchers.IO) {
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> awaitResultList(): RESULT = withContext(Dispatchers.IO) {
        awaitImpl().resultList<T, RESULT>()
    }

    @OptIn(InternalAPI::class)
    fun async(): Deferred<Response> = scope.async(Dispatchers.IO) {
        awaitImpl()
    }

    @OptIn(InternalAPI::class)
    inline fun <reified T, reified RESULT: CxHttpResult<T>> asyncResult(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(InternalAPI::class)
    inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> asyncResultList(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().resultList<T, RESULT>()
    }

    @OptIn(InternalAPI::class)
    suspend fun asFlow(): Flow<Response> = flow {
        emit(awaitImpl())
    }.flowOn(Dispatchers.IO)

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T, reified RESULT: CxHttpResult<T>> resultAsFlow(): Flow<RESULT> = flow {
        emit(awaitImpl().result<T, RESULT>())
    }.flowOn(Dispatchers.IO)

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> resultListAsFlow(): Flow<RESULT> = flow {
        emit(awaitImpl().resultList<T, RESULT>())
    }.flowOn(Dispatchers.IO)

    @InternalAPI
    suspend fun awaitImpl(): Response {
        var response = try {
            if (!request.reCall) {//避免重新请求时多次调用
                request.block()
            }
            // Hook and Execute request
            request.let {
                CxHttpHelper.applyHookRequest(it)
                CxHttpHelper.call.await(it)
            }
        } catch (ie: IOException) {
            val failInfo = CxHttpHelper.exToFailInfo(ie)
            Response(failInfo.code, failInfo.msg, null)
        }
        response.client = this
        response = CxHttpHelper.applyHookResponse(response)
        if (response.reCall) {
            return awaitImpl()
        }
        return response
    }

}
