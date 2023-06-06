# cxhttp

Coroutine Extensions Http（协程扩展Http）

- 支持get、post（可指定RequestBodyConverter）、postForm、postFile（可指定content-type）请求

- 支持自定义RequestBodyConverter和ResponseConverter，默认实现JacksonConverter

  ```kotlin
  interface RequestBodyConverter {
      val contentType: String
      fun <T> convert(value: T): ByteArray
  }
  interface ResponseConverter {
      val resultClass: Class<*>
      fun <T, RESULT: CxHttpResult<T>> convert(response: Response, tType: Type): RESULT
      fun <T, RESULT: CxHttpResult<List<T>>> convert(response: Response, listType: ParameterizedType): RESULT
  }
  ```
  
- 支持每个请求单独指定ResponseConverter，请求结果CxHttpResult<T>直接返回所需对象，代码简洁，扩展性强

  ```kotlin
  /**
   * CxHttp所有请求返回的结果基类，T为任意类型，默认实现 @see HttpResult
   * 调用者可实现自己的基类，属性名称无限制，但构造器（参数顺序及个数）必须包含与CxHttpResult一致的构造器
   * 例：data class MyHttpResult<T>(val code: Int/String,
   *                            val errorMsg: String,
   *                            val data: T?,): CxHttpResult<T>(code.toString(), errorMsg, data)
   * */
  abstract class CxHttpResult<T>(internal val cxCode: String, internal val cxMsg: String, internal val cxData: T?)
  ```

- 支持hookRequest（添加公共头信息、参数等）和hookResult（预处理请求结果，例如token失效自动刷新并重试功能、制作假数据测试等等）功能

- 支持自定义CxHttpCall，默认实现OkHttp3Call

# 示例

添加gradle依赖

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.zicheng2019:cxhttp:1.0.0")
}
```

代码调用

```kotlin
    val jacksonConverter = JacksonConverter(MyHttpResult::class.java)
    CxHttpHelper.init(scope = MainScope(), debugLog = true, call = MyHttpCall(), converter = jacksonConverter)
    
    CxHttpHelper.hookRequest{ request ->
        request.param("id", "123456")
        request.header("token", "1a2b3c4d5e6f")
        request
    }
    CxHttpHelper.hookResult{ result ->
        result.request
        result.setReRequest(false)//设置是否重新请求，默认false
        result
    }
    runBlocking {
        val job = CxHttp.get("https://www.baidu.com")
            //此处可指定协程，不指定默认使用CxHttpHelper.scope
            .launch(String::class.java, this){ resultGet1 ->
            println("resultGet1: $resultGet1")
        }
        val resultGet2: MyHttpResult<String> = CxHttp.get("https://www.baidu.com").await(String::class.java)
        println("resultGet2: $resultGet2")

        val resultDeferred = CxHttp.get("https://www.baidu.com").async<String, MyHttpResult<String>>(String::class.java)
        val resultGet3 = resultDeferred.await()
        println("resultGet3: $resultGet3")

        //reqBodyConverter可单独自定义，实现RequestBodyConverter接口即可，默认使用CxHttpHelper.init()指定的全局converter
        CxHttp.post(TEST_URL_USER_UPDATE, paramMap= mapOf(
            "name" to "zhangzicheng",
            "age" to 32,
            "gender" to "男",
            "occupation" to "农民"), reqBodyConverter = jacksonConverter)
            .launch<UserInfo, MyHttpResult<UserInfo>>(UserInfo::class.java){ resultPost1 ->
                println("resultPost1: $resultPost1")
            }
        CxHttp.post(TEST_URL_USER_UPDATE, paramEntity = UserInfo("zhangzicheng", 32, "男", "农民"))
            .launch<UserInfo, MyHttpResult<UserInfo>>(UserInfo::class.java){ resultPost2 ->
                println("resultPost2: $resultPost2")
            }
        CxHttp.post(TEST_URL_USER_PROJECTS)
            .param("page", 1)
            .param("pageSize", 2)
            .launchToList<ProjectInfo, MyHttpResult<List<ProjectInfo>>>(ProjectInfo::class.java){ resultPost3 ->
                println("resultPost3: $resultPost3")
            }
    }
```

# License
Copyright 2023 zicheng2019

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
