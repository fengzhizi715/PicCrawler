package com.cv4j.piccrawler

import com.cv4j.piccrawler.download.strategy.FileStrategy

/**
 * Created by tony on 2017/11/16.
 */
class CrawlerWrapper {

    var url:String? = null

    var ua:String? = null

    var referer:String? = null

    var autoReferer:Boolean = false

    var timeOut:Int = 0

    var repeat:Int = 0

    var fileStrategy:FileStrategy? = null
}

fun downloadPic(init: CrawlerWrapper.() -> Unit) {
    val wrap = CrawlerWrapper()

    wrap.init()

    doDownloadPic(wrap)
}

private fun doDownloadPic(wrap: CrawlerWrapper) {

    if (wrap.autoReferer && wrap.referer.isNullOrBlank()) {
        PicCrawlerClient.get()
                .ua(wrap.ua)
                .autoReferer()
                .timeOut(wrap.timeOut)
                .repeat(wrap.repeat)
                .fileStrategy(wrap.fileStrategy)
                .build()
                .downloadPic(wrap.url)
    } else {
        PicCrawlerClient.get()
                .ua(wrap.ua)
                .referer(wrap.referer)
                .timeOut(wrap.timeOut)
                .repeat(wrap.repeat)
                .fileStrategy(wrap.fileStrategy)
                .build()
                .downloadPic(wrap.url)
    }
}

fun downloadWebPageImages(init: CrawlerWrapper.() -> Unit) {

    val wrap = CrawlerWrapper()

    wrap.init()

    doDownloadWebPageImages(wrap)
}

private fun doDownloadWebPageImages(wrap: CrawlerWrapper) {

    if (wrap.autoReferer && wrap.referer.isNullOrBlank()) {
        PicCrawlerClient.get()
                .ua(wrap.ua)
                .autoReferer()
                .timeOut(wrap.timeOut)
                .fileStrategy(wrap.fileStrategy)
                .build()
                .downloadWebPageImages(wrap.url)
    } else {
        PicCrawlerClient.get()
                .ua(wrap.ua)
                .referer(wrap.ua)
                .timeOut(wrap.timeOut)
                .fileStrategy(wrap.fileStrategy)
                .build()
                .downloadWebPageImages(wrap.url)
    }

}
