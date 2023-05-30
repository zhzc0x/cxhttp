package com.zicheng.net.cxhttp.entity

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class ParameterizedTypeImpl(private val owner: Type? = null, private val raw: Class<*>,
                            vararg args: Type): ParameterizedType {

    constructor(raw: Class<*>, vararg args: Type): this(null, raw, *args)

    private val args: Array<Type>

    init {
        this.args = arrayOf(*args)
    }

    override fun getActualTypeArguments(): Array<Type> {
        return args
    }

    override fun getRawType(): Type {
        return raw
    }

    override fun getOwnerType(): Type? {
        return owner
    }
}