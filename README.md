### 1、 具体操作请查看工程根目录下《华视身份证读卡器（OTG）接口说明.docx》文档



### 2、依赖Library
从远程依赖：

在根目录的build.gradle中加入
```gradle
allprojects {
    repositories {
		...
        maven { url 'https://jitpack.io' }
    }
}
```
在主项目app的build.gradle中依赖
```gradle
dependencies {
    ...
    implementation 'com.github.DHSH:HUASHI:0.0.6'
}
```