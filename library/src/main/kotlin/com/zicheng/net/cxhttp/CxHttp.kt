package com.zicheng.net.cxhttp

import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.response.CxHttpResult
import com.zicheng.net.cxhttp.response.Response
import com.zicheng.net.cxhttp.request.*
import com.zicheng.net.cxhttp.response.result
import com.zicheng.net.cxhttp.response.resultList
import kotlinx.coroutines.*
import java.io.IOException


/**
 * Coroutine Extensions Http（协程扩展Http）
 *
 * */
class CxHttp private constructor(private val request: Request, private val block: suspend Request.() -> Unit) {

    companion object{

        fun get(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.GET.value, block)
        }

        fun head(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.HEAD.value, block)
        }

        fun post(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.POST.value, block)
        }

        fun delete(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.DELETE.value, block)
        }

        fun put(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.PUT.value, block)
        }

        fun patch(url: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return request(url, Request.Method.PATCH.value, block)
        }

        fun request(url: String, method: String, block: suspend Request.() -> Unit = {}): CxHttp{
            return CxHttp(Request(url, method), block)
        }

    }

    @CxHttpHelper.InternalAPI
    var scope: CoroutineScope = CxHttpHelper.scope

    private var respConverter: ResponseConverter = CxHttpHelper.converter

    @OptIn(CxHttpHelper.InternalAPI::class)
    fun scope(scope: CoroutineScope) = apply {
        this.scope = scope
    }

    /**
     * 设置ResponseConverter，自定义转换Response to CxHttpResult，默认使用CxHttpHelper.converter
     *
     * */
    fun respConverter(respConverter: ResponseConverter) = apply {
        this.respConverter = respConverter
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun launch(crossinline responseBlock: suspend CoroutineScope.(Response) -> Unit) = scope.launch {
        responseBlock(await())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <T, reified RESULT: CxHttpResult<T>> launchResult(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResult())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <T, reified RESULT: CxHttpResult<List<T>>> launchResultList(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResultList())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend fun await(): Response = withContext(Dispatchers.IO){
        awaitImpl()
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <T, reified RESULT: CxHttpResult<T>> awaitResult(): RESULT = withContext(Dispatchers.IO){
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <T, reified RESULT: CxHttpResult<List<T>>> awaitResultList(): RESULT = withContext(Dispatchers.IO){
        awaitImpl().resultList<T, RESULT>()
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    fun async(): Deferred<Response> = scope.async(Dispatchers.IO) {
        awaitImpl()
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <T, reified RESULT: CxHttpResult<T>> asyncResult(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <T, reified RESULT: CxHttpResult<List<T>>> asyncResultList(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().resultList<T, RESULT>()
    }

    @CxHttpHelper.InternalAPI
    suspend fun awaitImpl(): Response {
        var response = try {
            request.block()
            // Hook and Execute request
            CxHttpHelper.call.await(CxHttpHelper.applyHookRequest(request))
        } catch (ie: IOException) {
            Response(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ie), null)
        }
        response.converter = respConverter
        response.request = request
        response = CxHttpHelper.applyHookResponse(response)
        if(response.reRequest){
            return await()
        }
        return response
    }

}