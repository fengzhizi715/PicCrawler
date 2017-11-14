# PicCrawler

[![@Tony沈哲 on weibo](https://img.shields.io/badge/weibo-%40Tony%E6%B2%88%E5%93%B2-blue.svg)](http://www.weibo.com/fengzhizi715)
[ ![Download](https://api.bintray.com/packages/fengzhizi715/maven/crawler/images/download.svg) ](https://bintray.com/fengzhizi715/maven/crawler/_latestVersion)
[![License](https://img.shields.io/badge/license-Apache%202-lightgrey.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

用于抓取图片的爬虫，支持一些简单的定制比如User-Agent、referer、header、cookies等

最初是为了批量抓取图片验证码，然后导入tensorflow进行训练，才有了这个库。

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
compile 'com.cv4j.piccrawler:crawler:0.3.2'
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
  <version>0.3.2</version>
  <type>pom</type>
</dependency>
```

# 使用方法：
## 1.1 下载单张图片
1. 普通方式

```java
        String url = "..."; // 图片的地址
        CrawlerClient.get()
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
        CrawlerClient.get()
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

        CrawlerClient.get()
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
        CrawlerClient.get()
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
        CrawlerClient.get()
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
        CrawlerClient.get()
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
        CrawlerClient.get()
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
                .addProxy(new HttpHost("xxx.xx.xx.xx",xxxx))
                .addProxy(new HttpHost("xxx.xx.xx.xx",xxxx))
                .build()
                .downloadWebPageImages(url);
```
CrawlerClient也支持使用addProxyPool()传递一个代理列表。

## 1.6 针对防盗链的图片

如果图片有防盗链接，可以使用referer()方法，传入网站的网址。就可以愉快的下载图片了。

对于懒人还有一个方法autoReferer()，可以不必传入网站的网址。

# ChangeLog
[版本更新记录](CHANGELOG.md)