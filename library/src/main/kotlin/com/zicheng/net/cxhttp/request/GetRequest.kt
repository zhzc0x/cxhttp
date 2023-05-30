package com.zicheng.net.cxhttp.request

import okhttp3.Request.Builder

internal class GetRequest(url: String) : Request(url) {

    override fun toOkHttp3Request(): okhttp3.Request {
        val builder = if(params != null){
            val appendUrl = StringBuilder(url)
            val iterator = params!!.entries.iterator()
            appendUrl.append("?")
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                appendUrl.append(key).append("=").append(value)
                if (!iterator.hasNext()) {
                    break
                }
                appendUrl.append("&")
            }
            Builder().url(appendUrl.toString())
        } else {
            Builder().url(url)
        }
        headers?.forEach { builder.addHeader(it.key, it.value) }
        return builder.tag(tag).get().build()
    }

}