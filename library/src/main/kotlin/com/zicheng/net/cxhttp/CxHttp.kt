package com.zicheng.net.cxhttp

import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.entity.ParameterizedTypeImpl
import com.zicheng.net.cxhttp.request.*
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.reflect.Type


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
    inline fun <reified T, RESULT: CxHttpResult<T>> launch(
        crossinline awaitResult: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        awaitResult(await())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, RESULT: CxHttpResult<List<T>>> launchToList(
        crossinline awaitResult: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        awaitResult(awaitToList())
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <reified T, RESULT: CxHttpResult<T>> await(): RESULT = withContext(Dispatchers.IO){
        await(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    suspend inline fun <reified T, RESULT: CxHttpResult<List<T>>> awaitToList(): RESULT = withContext(Dispatchers.IO){
        awaitToList(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, RESULT: CxHttpResult<T>> async(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        await(T::class.java)
    }

    @OptIn(CxHttpHelper.InternalAPI::class)
    inline fun <reified T, RESULT: CxHttpResult<List<T>>> asyncToList(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitToList(T::class.java)
    }

    @CxHttpHelper.InternalAPI
    suspend fun <T, RESULT: CxHttpResult<T>> await(tClass: Class<T>): RESULT{
        var result = try {
            request.block()
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.applyHookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convert<T, RESULT>(response, tClass)
            } else {
                respConverter.convert(response.code.toString(), response.message)
            }
        } catch (ie: IOException) {
            respConverter.convert(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ie))
        }
        result.request = request
        result = CxHttpHelper.applyHookResult(result)
        if(result.reRequest){
            return await(tClass)
        }
        return result
    }

    @CxHttpHelper.InternalAPI
    suspend fun <T, RESULT: CxHttpResult<List<T>>> awaitToList(tClass: Class<T>): RESULT{
        var result = try {
            request.block()
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.applyHookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convert<T, RESULT>(response, ParameterizedTypeImpl(List::class.java, tClass as Type))
            } else {
                respConverter.convert(response.code.toString(), response.message)
            }
        } catch (ie: IOException) {
            respConverter.convert(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ie))
        }
        result.request = request
        result = CxHttpHelper.applyHookResult(result)
        if(result.reRequest){
            return awaitToList(tClass)
        }
        return result
    }

}