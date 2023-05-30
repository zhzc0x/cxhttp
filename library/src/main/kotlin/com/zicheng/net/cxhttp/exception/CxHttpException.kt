package com.zicheng.net.cxhttp.exception

import java.io.IOException

class CxHttpException(val ie: IOException): IOException(ie.message, ie.cause)