package com.zicheng.net.cxhttp

import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.request.*
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
    inline fun <reified T, RESULT: CxHttpResult<T>> launchResult(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResult())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, RESULT: CxHttpResult<List<T>>> launchResultList(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResultList())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <reified T, RESULT: CxHttpResult<T>> awaitResult(): RESULT = withContext(Dispatchers.IO){
        awaitResult(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <reified T, RESULT: CxHttpResult<List<T>>> awaitResultList(): RESULT = withContext(Dispatchers.IO){
        awaitResultList(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, RESULT: CxHttpResult<T>> asyncResult(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitResult(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, reified RESULT: CxHttpResult<List<T>>> asyncResultList(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitResultList(T::class.java)
    }

    @CxHttpHelper.InternalAPI
    suspend fun <T, RESULT: CxHttpResult<T>> awaitResult(tClass: Class<T>): RESULT{
        var result = try {
            request.block()
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.applyHookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convertResult<T, RESULT>(response.body, tClass)
            } else {
                respConverter.convertResult(response.code.toString(), response.message)
            }
        } catch (ex: Exception) {
            respConverter.convertResult(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ex))
        }
        result.request = request
        result = CxHttpHelper.applyHookResult(result)
        if(result.reRequest){
            return awaitResult(tClass)
        }
        return result
    }

    @CxHttpHelper.InternalAPI
    suspend fun <T, RESULT: CxHttpResult<List<T>>> awaitResultList(tClass: Class<T>): RESULT{
        var result = try {
            request.block()
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.applyHookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convertResultList<T, RESULT>(response.body, tClass)
            } else {
                respConverter.convertResult(response.code.toString(), response.message)
            }
        } catch (ex: Exception) {
            respConverter.convertResult(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ex))
        }
        result.request = request
        result = CxHttpHelper.applyHookResult(result)
        if(result.reRequest){
            return awaitResultList(tClass)
        }
        return result
    }

}