package com.zicheng.net.cxhttp.entity

import com.fasterxml.jackson.core.type.TypeReference
import java.lang.reflect.Type

class JacksonType<T>(private val type: Type) : TypeReference<T>() {
    override fun getType(): Type {
        return type
    }
}