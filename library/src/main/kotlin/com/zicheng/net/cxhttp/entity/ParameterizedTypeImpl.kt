package com.zicheng.net.cxhttp.entity

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class ParameterizedTypeImpl(private val ownerType: Type? = null, private val rawType: Class<*>,
                            vararg typeArguments: Type): ParameterizedType {

    constructor(rawType: Class<*>, vararg typeArguments: Type): this(null, rawType, *typeArguments)

    private val typeArguments: Array<Type>

    init {
        this.typeArguments = arrayOf(*typeArguments)
    }

    override fun getActualTypeArguments(): Array<Type> {
        return typeArguments
    }

    override fun getRawType(): Type {
        return rawType
    }

    override fun getOwnerType(): Type? {
        return ownerType
    }
}