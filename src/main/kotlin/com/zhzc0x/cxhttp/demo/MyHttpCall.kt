package com.zhzc0x.cxhttp.demo

import JSON_PROJECTS
import JSON_TOKEN_INFO
import JSON_USER_INFO
import TEST_URL_TOKEN_REFRESH
import TEST_URL_USER_PROJECTS
import TEST_URL_USER_UPDATE
import cxhttp.call.CxHttpCall
import cxhttp.response.Response
import cxhttp.request.Request

class MyHttpCall(private val httpCall: CxHttpCall): CxHttpCall by httpCall {

    override suspend fun await(request: Request): Response {
        if(request.url != TEST_URL_TOKEN_REFRESH && request.headers?.get("token").isNullOrEmpty()){
            return Response(401, "token 无效", null)
        }
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
            TEST_URL_TOKEN_REFRESH -> {
                return Response(200, "", object: Response.Body(){
                    override fun string(): String {
                        return JSON_TOKEN_INFO
                    }
                })
            }
            else -> {
                return httpCall.await(request)
            }
        }
    }

}