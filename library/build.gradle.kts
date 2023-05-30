plugins {
    kotlin("jvm")
}

group = "com.github.zicheng2019"

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.9.3")//最新版本不兼容Android4.4
    api("com.squareup.okhttp3:logging-interceptor:4.9.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.1")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
}

tasks.test {
    useJUnitPlatform()
}