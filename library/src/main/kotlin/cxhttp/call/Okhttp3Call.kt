package cxhttp.call

import cxhttp.response.Response
import cxhttp.request.Request
import cxhttp.request.buildOkhttp3Request
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class Okhttp3Call(private var _okHttpClient: OkHttpClient? = null,
                  onConfiguration: OkHttpClient.Builder.() -> Unit = {}): CxHttpCall {

    private val okHttpClient: OkHttpClient
        get() = _okHttpClient!!

    init {
        if (_okHttpClient == null) {
            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.onConfiguration()
            _okHttpClient = clientBuilder.build()
        }
    }

    override suspend fun await(request: Request): Response {
        val realCall = okHttpClient.newCall(request.buildOkhttp3Request())
        return suspendCancellableCoroutine { continuation ->
            realCall.enqueue(object : Callback {
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (continuation.isCancelled) {
                        return
                    }
                    continuation.resume(Response(response.code, response.message, response.body?.let {
                        object: Response.Body() {
                            override fun string(): String {
                                return it.string()
                            }
                            override fun bytes(): ByteArray {
                                return it.bytes()
                            }
                            override fun charStream(): Reader {
                                return it.charStream()
                            }
                            override fun byteStream(): InputStream {
                                return it.byteStream()
                            }
                        }
                    }))
                }
                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if it is already cancelled.
                    if (continuation.isCancelled) {
                        return
                    }
                    continuation.resumeWithException(e)
                }
            })
            continuation.invokeOnCancellation {
                realCall.cancel()
            }
        }
    }

}