package com.zicheng.demo.cxhttp

import JSON_PROJECTS
import JSON_USER_INFO
import TEST_URL_USER_PROJECTS
import TEST_URL_USER_UPDATE
import cxhttp.call.CxHttpCall
import cxhttp.response.Response
import cxhttp.request.Request

class MyHttpCall(private val httpCall: CxHttpCall): CxHttpCall by httpCall {

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
                return httpCall.await(request)
            }
        }
    }

}