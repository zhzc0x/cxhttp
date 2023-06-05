package com.zicheng.net.cxhttp

import com.zicheng.net.cxhttp.converter.RequestBodyConverter
import com.zicheng.net.cxhttp.converter.ResponseConverter
import com.zicheng.net.cxhttp.entity.CxHttpResult
import com.zicheng.net.cxhttp.entity.ParameterizedTypeImpl
import com.zicheng.net.cxhttp.request.*
import com.zicheng.net.cxhttp.request.GetRequest
import com.zicheng.net.cxhttp.request.PostFormRequest
import com.zicheng.net.cxhttp.request.PostRequest
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Type


/**
 * Coroutine Extensions Http（协程扩展Http）
 *
 * */
class CxHttp private constructor(private val request: Request) {

    private var respConverter: ResponseConverter = CxHttpHelper.converter

    companion object{

        /**
         * GET Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param paramMap Map<String, Any>?：请求参数
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun get(url: String, headerMap: Map<String, String>? = null, paramMap: Map<String, Any>? = null): CxHttp {
            val request = GetRequest(url).apply(headerMap, paramMap)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param paramMap Map<String, Any>?：请求参数
         * @param reqBodyConverter RequestBodyConverter：可自定义RequestBody，默认使用CxHttpHelper.converter
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun post(url: String, headerMap: Map<String, String>? = null, paramMap: Map<String, Any>? = null,
                 reqBodyConverter: RequestBodyConverter = CxHttpHelper.converter): CxHttp {
            val request = PostRequest(url, reqBodyConverter).apply(headerMap, paramMap)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param paramEntity Any：实体对象请求参数
         * 此方法无法添加公共参数，其它同上
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun post(url: String, headerMap: Map<String, String>? = null, paramEntity: Any,
                 reqBodyConverter: RequestBodyConverter = CxHttpHelper.converter): CxHttp {
            val request = PostRequest(url, reqBodyConverter, paramEntity).apply(headerMap, null)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param paramMap Map<String, Any>?：请求参数
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun postForm(url: String, headerMap: Map<String, String>? = null, paramMap: Map<String, Any>? = null): CxHttp {
            val request = PostFormRequest(url).apply(headerMap, paramMap)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param paramMap Map<String, Any>?：请求参数
         * @param fileKey String：文件key
         * @param filePathList List<String>：文件路径List<path>
         * @param onProgress ((Long, Long) -> Unit)?：totalLength, currentLength
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun postForm(url: String, headerMap: Map<String, String>? = null, paramMap: Map<String, Any>? = null,
                     fileKey: String, filePathList: List<String>, onProgress: ((Long, Long) -> Unit)? = null): CxHttp {
            val request = PostFormRequest(url, onProgress).apply(headerMap, paramMap)
            request.file(fileKey, filePathList)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param paramMap Map<String, Any>?：请求参数
         * @param fileKey String：文件key
         * @param filePathMap Map<String, String>：文件路径Map<name, path>
         * @param onProgress ((Long, Long) -> Unit)?：totalLength, currentLength
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun postForm(url: String, headerMap: Map<String, String>? = null, paramMap: Map<String, Any>? = null,
                     fileKey: String, filePathMap: Map<String, String>, onProgress: ((Long, Long) -> Unit)? = null): CxHttp {
            val request = PostFormRequest(url, onProgress).apply(headerMap, paramMap)
            request.file(fileKey, filePathMap)
            return CxHttp(request)
        }

        /**
         * POST Method
         * @param url String：请求地址
         * @param headerMap Map<String, String>?：请求头信息
         * @param file File：上传文件
         * @param type String：文件类型，例“application/zip”，“image/jpeg”，默认application/octet-stream
         *
         * */
        @JvmStatic
        @JvmOverloads
        fun postFile(url: String, headerMap: Map<String, String>? = null, file: File,
                     type: String = CxHttpHelper.CONTENT_TYPE_OCTET_STREAM): CxHttp {
            val request = PostFileRequest(url).apply(headerMap, null)
            request.file(file, type)
            return CxHttp(request)
        }
    }

    fun header(name: String, value: String) = apply {
        request.header(name, value)
    }

    fun param(name: String, value: Any) = apply {
        request.param(name, value)
    }

    fun tag(tag: Any) = apply {
        request.tag(tag)
    }

    /**
     * 设置ResponseConverter，自定义转换Response to CxHttpResult，默认使用CxHttpHelper.converter
     *
     * */
    fun respConverter(respConverter: ResponseConverter) = apply {
        this.respConverter = respConverter
    }

    @JvmOverloads
    fun <T, RESULT: CxHttpResult<T>> launch(clazz: Class<T>, coroutineScope: CoroutineScope = CxHttpHelper.scope,
                                            awaitResult: suspend CoroutineScope.(RESULT) -> Unit)
    = coroutineScope.launch {
        awaitResult(await(clazz))
    }

    @JvmOverloads
    fun <T, RESULT: CxHttpResult<List<T>>> launchToList(clazz: Class<T>, coroutineScope: CoroutineScope = CxHttpHelper.scope,
                                                        awaitResult: suspend CoroutineScope.(RESULT) -> Unit)
    = coroutineScope.launch {
        awaitResult(awaitToList(clazz))
    }

    suspend fun <T, RESULT: CxHttpResult<T>> await(clazz: Class<T>): RESULT = withContext(Dispatchers.IO){
        awaitImpl(clazz)
    }

    suspend fun <T, RESULT: CxHttpResult<List<T>>> awaitToList(clazz: Class<T>): RESULT = withContext(Dispatchers.IO){
        awaitToListImpl(clazz)
    }

    @JvmOverloads
    fun <T, RESULT: CxHttpResult<T>> async(clazz: Class<T>, coroutineScope: CoroutineScope = CxHttpHelper.scope): Deferred<RESULT>
    = coroutineScope.async(Dispatchers.IO) {
        awaitImpl(clazz)
    }

    @JvmOverloads
    fun <T, RESULT: CxHttpResult<List<T>>> asyncToList(clazz: Class<T>, coroutineScope: CoroutineScope = CxHttpHelper.scope): Deferred<RESULT>
    = coroutineScope.async(Dispatchers.IO) {
        awaitToListImpl(clazz)
    }

    private suspend fun <T, RESULT: CxHttpResult<T>> awaitImpl(clazz: Class<T>): RESULT{
        var result = try {
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.hookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convert<T, RESULT>(response, clazz)
            } else {
                respConverter.convert(response.code.toString(), response.message)
            }
        } catch (ie: IOException) {
            respConverter.convert(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ie))
        }
        result.request = request
        result = CxHttpHelper.hookResult(result)
        if(result.reRequest){
            return awaitImpl(clazz)
        }
        return result
    }

    private suspend fun <T, RESULT: CxHttpResult<List<T>>> awaitToListImpl(clazz: Class<T>): RESULT{
        var result = try {
            // Hook and Execute request
            val response = CxHttpHelper.call.await(CxHttpHelper.hookRequest(request))
            if(response.isSuccessful && response.body != null){
                respConverter.convert<T, RESULT>(response, ParameterizedTypeImpl(List::class.java, clazz as Type))
            } else {
                respConverter.convert(response.code.toString(), response.message)
            }
        } catch (ie: IOException) {
            respConverter.convert(CxHttpHelper.FAILURE_CODE, CxHttpHelper.exToMessage(ie))
        }
        result.request = request
        result = CxHttpHelper.hookResult(result)
        if(result.reRequest){
            return awaitToListImpl(clazz)
        }
        return result
    }

}