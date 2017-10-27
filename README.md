# PicCrawler

[ ![Download](https://api.bintray.com/packages/fengzhizi715/maven/crawler/images/download.svg) ](https://bintray.com/fengzhizi715/maven/crawler/_latestVersion)

用于抓取图片的爬虫，支持一些简单的定制

最初是为了批量抓取图片验证码，然后导入tensorflow进行训练，所以有了这个库。

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
compile 'com.cv4j.piccrawler:crawler:0.2.0'
```


Maven:

```xml
<dependency>
  <groupId>com.cv4j.piccrawler</groupId>
  <artifactId>crawler</artifactId>
  <version>0.2.0</version>
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
                .downloadWebPageImages(url);
```