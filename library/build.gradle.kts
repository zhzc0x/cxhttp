plugins {
    kotlin("jvm")
}
apply(from="${rootDir}/library/upload-archives.gradle")

group = "com.zicheng.net.cxhttp"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.2")
    compileOnly("com.squareup.okhttp3:okhttp:4.9.3")
    compileOnly("com.squareup.okhttp3:logging-interceptor:4.9.3")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    compileOnly("com.google.code.gson:gson:2.10.1")
}
