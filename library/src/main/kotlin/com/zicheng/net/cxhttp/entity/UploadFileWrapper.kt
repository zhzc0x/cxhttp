package com.zicheng.net.cxhttp.entity

import java.io.File
import java.net.URI

internal class UploadFileWrapper: File {

    private var value: String? = null //用于文件上传时对应的value值, 为空时,默认为文件名
    private val key: String

    constructor(key: String, pathname: String): super(pathname){
        this.key = key
    }

    constructor(key: String, parent: String, child: String): super(parent, child) {
        this.key = key
    }

    constructor(key: String, uri: URI): super(uri) {
        this.key = key
    }

    fun setValue(value: String) {
        this.value = value
    }

    fun getKey(): String{
        return key
    }

    fun getValue(): String {
        return if (value != null) value!! else name
    }

    override fun toString(): String {
        return "UploadFileWrapper{" +
                "path='" + path + '\'' +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                "} " + super.toString()
    }

}