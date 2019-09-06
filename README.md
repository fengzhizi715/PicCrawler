# PicCrawler

[![@Tony沈哲 on weibo](https://img.shields.io/badge/weibo-%40Tony%E6%B2%88%E5%93%B2-blue.svg)](http://www.weibo.com/fengzhizi715)
[ ![Download](https://api.bintray.com/packages/fengzhizi715/maven/crawler/images/download.svg) ](https://bintray.com/fengzhizi715/maven/crawler/_latestVersion)
[![License](https://img.shields.io/badge/license-Apache%202-lightgrey.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

用于抓取图片的爬虫，支持一些简单的定制比如User-Agent、referer、header、cookies等

最初是为了批量抓取图片验证码，然后导入tensorflow进行训练，才有了这个库。

它的结构如下图所示
![](PicCrawlerClient.png)

# 下载安装:

对于Java项目如果使用gradle构建，由于默认不是使用jcenter，需要在相应module的build.gradle中配置

```groovy
repositories {
    mavenCentral()
    jcenter()
}
```
Gradle:

```groovy
implementation 'com.cv4j.piccrawler:crawler:1.0.0'
```


Maven:

当项目使用Maven进行构建时，在pom.xml文件中添加以下库

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```

然后添加如下的依赖
```xml
<dependency>
  <groupId>com.cv4j.piccrawler</groupId>
  <artifactId>crawler</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

# 使用方法：
## 1.1 下载单张图片
1. 普通方式

```java
        String url = "..."; // 图片的地址
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .repeat(200) // 重复200次
                .build()
                .downloadPic(url);
```

2. 使用RxJava

```java
        String url = "..."; // 图片的地址
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .repeat(200)
                .build()
                .downloadPicUseRx(url);
```

3. 使用RxJava，下载之后的图片还能做后续的处理

```java
        String url = "..."; // 图片的地址

        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .repeat(20)
                .build()
                .downloadPicToFlowable(url)
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        // do something
                    }
                });
```

## 1.2 下载多张图片
```java
        List<String> urls = ...; // 多张图片地址的集合
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .build()
                .downloadPics(urls);
```

## 1.3 下载某个网页的全部图片
```java
        String url = "..."; // 针对某一个网址
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .build()
                .downloadWebPageImages(url);
```

## 1.4 下载多个网页的全部图片
```java
        List<String> urls = ...; // 多个网页的集合
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .build()
                .downloadWebPageImages(urls);
```

## 1.5 通过代理使用图片爬虫
如果有多个代理的话，内部使用轮询的方式切换代理。

```java
        String url = ...; // 针对某一个网址
        PicCrawlerClient.get()
                .timeOut(6000)
                .fileStrategy(new FileStrategy() {

                    @Override
                    public String filePath() {
                        return "temp";
                    }

                    @Override
                    public String picFormat() {
                        return "png";
                    }

                    @Override
                    public FileGenType genType() {

                        return FileGenType.AUTO_INCREMENT;
                    }
                })
                .addProxy(new Proxy("xxx.xx.xx.xx",xxxx))
                .addProxy(new Proxy("xxx.xx.xx.xx",xxxx))
                .build()
                .downloadWebPageImages(url);
```
CrawlerClient也支持使用addProxyPool()传递一个代理列表。

爬虫在工作中，如果发现代理不可用内部会有策略会代理池中丢弃该不可用的代理。

## 1.6 针对防盗链的图片

如果图片有防盗链接，可以使用referer()方法，传入网站的网址。就可以愉快的下载图片了。

对于懒人还有一个方法autoReferer()，可以不必传入网站的网址。

## 1.7 增加对Kotlin的支持,可以使用DSL的方式来使用图片爬虫

1. 下载单张图片

```kotlin
fun main(args: Array<String>) {

    downloadPic {
        url = "..."
        timeOut = 6000
        repeat = 20
    }
}
```

2. 下载某个网页的全部图片

```kotlin
fun main(args: Array<String>) {

    downloadWebPageImages {

        url = "..."
        timeOut = 6000
        autoReferer = true
    }
}
```

## 1.8 增加了通过Selenium模拟页面滚动来抓取网页图片

需要使用Selenium的话，在单独的crawler-selenium module中

```java
SeleniumCrawlerClient client = new SeleniumCrawlerClient();
client.downloadPic("...",3);
``` 

在这里webdriver采用chromedriver，对于不同的操作系统chromedriver需要使用对应的版本。    


# 专业的爬虫

笔者开发的专业的爬虫框架: [NetDiscovery](https://github.com/fengzhizi715/NetDiscovery)

# ChangeLog
[版本更新记录](CHANGELOG.md)


# 联系方式:

QQ交流群：490882934

> Java与Android技术栈：每周更新推送原创技术文章，欢迎扫描下方的公众号二维码并关注，期待与您的共同成长和进步。

![](https://github.com/fengzhizi715/NetDiscovery/blob/master/images/gzh.jpeg)


License
-------

    Copyright (C) 2017 Tony Shen.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
