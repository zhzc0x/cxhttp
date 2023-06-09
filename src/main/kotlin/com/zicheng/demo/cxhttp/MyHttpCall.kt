package com.zicheng.demo.cxhttp

import JSON_PROJECTS
import JSON_USER_INFO
import TEST_URL_USER_PROJECTS
import TEST_URL_USER_UPDATE
import com.zicheng.net.cxhttp.call.CxHttpCall
import com.zicheng.net.cxhttp.call.OkHttp3Call
import com.zicheng.net.cxhttp.entity.Response
import com.zicheng.net.cxhttp.exception.CxHttpException
import com.zicheng.net.cxhttp.request.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class MyHttpCall: CxHttpCall {

    private val okHttp3Call = OkHttp3Call{
        callTimeout(15, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }

    @Throws(CxHttpException::class)
    override suspend fun await(request: Request): Response {
        when (request.url) {
            TEST_URL_USER_UPDATE -> {
                return Response(200, "", object: Response.Body(){
                    override fun string(): String {
                        return JSON_USER_INFO
                    }
                })
            }
            TEST_URL_USER_PROJECTS -> {
                return Response(200, "", object: Response.Body(){
                    override fun string(): String {
                        return JSON_PROJECTS
                    }
                })
            }
            else -> {
                return okHttp3Call.await(request)
            }
        }
    }

}