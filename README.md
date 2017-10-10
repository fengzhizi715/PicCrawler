# PicCrawler
用于抓取图片的爬虫，支持一些简单的定制

最初是为了批量抓取图片验证码，然后导入tensorflow进行训练，所以有了这个库。

使用方法：

```java
        String url = "..." // 图片的地址
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
                .downloadPic(url);
```

下载图片的方法也可以使用RxJava

```java
        String url = "..." // 图片的地址
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
                .downloadPicUseRx(url);
```

稍后，我会加上支持抓取多个图片url的方法。

