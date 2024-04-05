
import com.zhzc0x.cxhttp.demo.bean.MyHttpResult
import com.zhzc0x.cxhttp.demo.bean.ProjectInfo
import com.zhzc0x.cxhttp.demo.bean.UserInfo
import com.zhzc0x.cxhttp.demo.MyHttpCall
import com.zhzc0x.cxhttp.demo.bean.TokenInfo
import cxhttp.CxHttp
import cxhttp.CxHttpHelper
import cxhttp.call.Okhttp3Call
import cxhttp.converter.JacksonConverter
import cxhttp.response.CxHttpResult
import cxhttp.response.body
import cxhttp.response.bodyOrNull
import cxhttp.response.result
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit


const val JSON_USER_INFO = "{\"code\":200,\n" +
        "\"errorMsg\":\"请求成功\",\n" +
        "\"data\":{\n" +
        "\"id\":\"001\",\n" +
        "\"name\":\"小张\",\n" +
        "\"age\":\"32\",\n" +
        "\"gender\":\"男\",\n" +
        "\"occupation\":\"搬砖\"\n" +
        "}\n" +
        "}"
const val JSON_PROJECTS = "{\n" +
        "    \"code\":200,\n" +
        "    \"errorMsg\":\"请求成功\",\n" +
        "    \"data\":[\n" +
        "        {\n" +
        "            \"name\":\"banner-android\",\n" +
        "            \"url\":\"https://github.com/zicheng2019/banner-android.git\",\n" +
        "            \"groupId\":\"banner-android\",\n" +
        "            \"artifactId\":\"com.github.zicheng2019\",\n" +
        "            \"version\":\"1.0.5\",\n" +
        "            \"mavenUrl\":\"https://jitpack.io\",\n" +
        "            \"desc\":\"Android Kotlin基于ViewPage2和ViewBinding的轻量级BannerView轮播图；简洁、高效、功能强大，一行代码轻松实现循环轮播，一屏三页任意变，Item样式任意定制扩展\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"name\":\"linechart-android\",\n" +
        "            \"url\":\"https://github.com/zicheng2019/linechart-android.git\",\n" +
        "            \"groupId\":\"linechart-android\",\n" +
        "            \"artifactId\":\"com.github.zicheng2019\",\n" +
        "            \"version\":\"1.0.4\",\n" +
        "            \"mavenUrl\":\"https://jitpack.io\",\n" +
        "            \"desc\":\"LineChartView（折线图，动态折线图）静态波形绘制View，LiveLineChartView（实时折线图）动态实时波形绘制View，简单易用\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"
const val JSON_TOKEN_INFO = "{\"code\":200,\n" +
        "\"errorMsg\":\"请求成功\",\n" +
        "\"data\":{\n" +
        "\"accessToken\":\"asd2fasd1sgsa4gasfas\",\n" +
        "\"refreshToken\":\"f4trgegrfg5wrqwsdfa0\",\n" +
        "\"age\":\"32\",\n" +
        "\"gender\":\"男\",\n" +
        "\"occupation\":\"搬砖\"\n" +
        "}\n" +
        "}"

const val TEST_URL_USER_UPDATE = "test://www.******.com/user/update"
const val TEST_URL_USER_PROJECTS = "test://www.******.com/user/projects"
const val TEST_URL_TOKEN_REFRESH = "test://www.******.com/token/refresh"

fun main(args: Array<String>) {
    val okhttp3Call = Okhttp3Call {
        callTimeout(15, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }
    val jacksonConverter = JacksonConverter()
    CxHttpHelper.init(scope=MainScope(), debugLog=true, call= MyHttpCall(okhttp3Call), converter=jacksonConverter)
    var tokenInfo: TokenInfo? = null
    //hookRequest可以添加一些公共参数和头信息
    CxHttpHelper.hookRequest { request ->
        request.header("token", tokenInfo?.accessToken ?: "")
        request.param("id", "123456")
    }

    val mutexLock = Mutex()
    //hookResponse可以预处理请求结果，例如token失效自动刷新并重试功能、制作假数据测试等等
    CxHttpHelper.hookResponse { response ->
        if (response.code == 401) {
            //加锁防止多次重复刷新
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
        }
        response
    }
    CxHttpHelper.hookResult { result: CxHttpResult<*> ->
        result as MyHttpResult
        if (result.code == 401) {
            //加锁防止多次重复刷新
            if (mutexLock.isLocked) {
                mutexLock.withLock {
                    result.setReCall()
                }
            } else {
                println("hookResult： token失效，准备刷新并重试")
                tokenInfo = refreshToken()
                result.setReCall()//设置重新请求
            }
        }
        result
    }
    runBlocking {
        val job = CxHttp.get("https://www.baidu.com")
            //此处可指定协程，不指定默认使用CxHttpHelper.scope
            .scope(this)
            .launch { response ->
                println("response1=$response")
                if(response.body != null){
                    println("resultGet1: ${response.body<String>()}")
                } else {
                    // TODO: Can do some exception handling
                }
        }
        job.join()
        val resultGet2 = CxHttp.get("https://www.baidu.com").await().bodyOrNull(String::class.java)
        println("resultGet2: $resultGet2")

        val resultDeferred = CxHttp.get("https://www.baidu.com").async()
        val resultGet3 = resultDeferred.await().result<String, MyHttpResult<String>>()
        println("resultGet3: $resultGet3")

        CxHttp.get(TEST_URL_USER_PROJECTS).resultListAsFlow<ProjectInfo, MyHttpResult<List<ProjectInfo>>>().collect {
            println("resultListAsFlow: ${it.data}")
        }

        tokenInfo = null
        CxHttp.post(TEST_URL_USER_UPDATE){
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
            println("resultPost1: ${resultPost1.data!!.occupation}")
        }
        CxHttp.post(TEST_URL_USER_PROJECTS) {
            param("page", 1)
            param("pageSize", 2)
        }.launchResultList<ProjectInfo, MyHttpResult<List<ProjectInfo>>> { resultPost2 ->
            println("resultPost2: $resultPost2")
        }

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

    }
}

private suspend fun refreshToken(): TokenInfo? {
    val tokenInfoResult: MyHttpResult<TokenInfo> = CxHttp.post(TEST_URL_TOKEN_REFRESH).awaitResult()
    return tokenInfoResult.data
}