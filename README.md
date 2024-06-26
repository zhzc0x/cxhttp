# cxhttp

Coroutine Extensions Http（协程扩展Http框架）

- 支持get、post（setBody(T，contentType，可单独指定RequestBodyConverter)、formBody、multipartBody）、delete、patch等请求

- 支持自定义RequestBodyConverter和ResponseConverter，默认实现JacksonConverter

  ```kotlin
  interface RequestBodyConverter {
      val contentType: String
      fun <T> convert(value: T, tType: Class<out T>): ByteArray
  }
  interface ResponseConverter {
      fun <T> convert(body: Response.Body, tType: Class<T>): T
      fun <T, RESULT: CxHttpResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>): RESULT
      fun <T, RESULT: CxHttpResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>): RESULT
  }
  ```
  
- 支持自定义请求结果类型，每个请求可以单独指定ResponseConverter

- 支持统一请求结果CxHttpResult<T>，T为自定义类型

  ```kotlin
  /**
   * CxHttp统一请求结果基类，T为任意类型，默认实现 @see HttpResult
   * 调用者可实现自己的基类，属性名称无限制，但构造器（参数顺序及个数）必须包含与CxHttpResult一致的构造器
   * 例：data class MyHttpResult<T>(val code: Int/String,
   *                            val errorMsg: String,
   *                            val data: T?,): CxHttpResult<T>(code.toString(), errorMsg, data)
   * */
  abstract class CxHttpResult<T>(internal val cxCode: String, internal val cxMsg: String, internal val cxData: T?)
  ```

- 支持HookRequest（添加公共头信息、参数等）和HookResponse、HookResult（预处理请求结果，例如token失效自动刷新并重试功能、制作假数据测试等等）功能

- 支持自定义CxHttpCall，默认实现Okhttp3Call

  ```kotlin
  interface CxHttpCall {
      suspend fun await(request: Request): Response
  }
  ```

- 支持okhttp3相关配置可直接通过Okhttp3Call配置，代码示例如下：

  ```kotlin
  val okhttp3Call = Okhttp3Call {
      callTimeout(15, TimeUnit.SECONDS)
      addInterceptor(CallServerInterceptor())
      ......
  }
  CxHttpHelper.init(call=okhttp3Call)
  ```

# 示例

添加gradle依赖

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.zhzc0x.cxhttp:cxhttp:1.2.4")
    //默认网络请求库Okhttp3Call，如果使用其它网络库可去掉
    implementation("com.squareup.okhttp3:okhttp:4.9.3")//最新版本不兼容Android4.4
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    //可选JacksonConverter()
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    //可选GsonConverter()
    implementation("com.google.code.gson:gson:2.10.1")
}
```

初始化全局配置

```kotlin
    val okhttp3Call = Okhttp3Call {
        callTimeout(15, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }    
	val jacksonConverter = JacksonConverter()
    CxHttpHelper.init(scope=MainScope(), debugLog=true, call=MyHttpCall(okhttp3Call), converter=jacksonConverter)
```

GET请求

```kotlin
    val job = CxHttp.get("https://www.baidu.com")
         //此处可指定协程，不指定默认使用CxHttpHelper.scope
        .scope(this).launch { response ->
            if (response.body != null) {
                println("resultGet1: ${response.body<String>()}")
            } else {
                // TODO: Can do some exception handling
            }	
        }
    val resultGet2 = CxHttp.get("https://www.baidu.com").await().bodyOrNull(String::class.java)
    println("resultGet2: $resultGet2")

    val resultDeferred = CxHttp.get("https://www.baidu.com").async()
    val resultGet3: String? = resultDeferred.await().bodyOrNull()
    println("resultGet3: $resultGet3")
```

POST请求

```kotlin
    CxHttp.post(TEST_URL_USER_UPDATE) {
        //You can set params or body
        params(mapOf(
            "name" to "zhangzicheng",
            "age" to 32,
            "gender" to "男",
            "occupation" to "农民"))
        setBody(UserInfo("zhangzicheng", 32, "男", "农民"), UserInfo::class.java)
        //可单独设置requestBodyConverter，自定义实现RequestBodyConverter接口即可，默认使用CxHttpHelper.init()设置的全局converter
        bodyConverter = jacksonConverter
    }.launchResult<UserInfo, MyHttpResult<UserInfo>> { resultPost1 ->
        println("resultPost1: $resultPost1")
    }
    CxHttp.post(TEST_URL_USER_PROJECTS){
        param("page", 1)
        param("pageSize", 2)
    }.launchResultList<ProjectInfo, MyHttpResult<List<ProjectInfo>>> { resultPost2 ->
        println("resultPost2: $resultPost2")
    }
```

POST文件

```kotlin
    CxHttp.post("Upload url") {
        delay(3000)
        setBody(File("filePath"), contentType = CxHttpHelper.CONTENT_TYPE_OCTET_STREAM)
        onProgress = { totalLength, currentLength ->
            // TODO: Update progress 
        }
    }.await()
```

POST form、multipart

```kotlin
    CxHttp.post("form url") {
        formBody {
            append("name", "value")
        }
    }.await().isSuccessful
    CxHttp.post("multipart url") {
        multipartBody {
            append("name", "value")
            append("name", "filename", "filepath")
            append("name", null, File("filepath"))
        }
    }.await().isSuccessful
```

HookRequest

```kotlin
    CxHttpHelper.hookRequest { request ->
        request.header("token", tokenInfo?.accessToken ?: "")
        request.param("id", "123456")
    }
```

HookResponse

```kotlin
    val mutexLock = Mutex()
    CxHttpHelper.hookResponse { response ->
        //可以加锁防止多次重复刷新
        if (!mutexLock.isLocked) {
            mutexLock.withLock {
                println("hookResponse： token失效，准备刷新并重试")
                tokenInfo = refreshToken()
                response.setReCall()//设置重新请求
            }
        } else {
            mutexLock.withLock {
                response.setReCall()
            }
        }    
        response
    }
```

### 高级用法

HookResult（Hook统一请求结果CxHttpResult<*>）

```kotlin
    CxHttpHelper.hookResult { result: CxHttpResult<*> ->
        result as MyHttpResult
        if (result.code == 401) {
            println("hookResult： token失效，准备刷新并重试")
            tokenInfo = refreshToken()
            result.setReCall()//设置重新请求
        }
        result
    }
```

### 混淆配置

```properties
#CxHttp
-keep class * extends cxhttp.response.CxHttpResult{*;}
-keep class cxhttp.converter.*{*;}
```



# License

Copyright 2023 zhzc0x

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
