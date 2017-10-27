package com.cv4j.piccrawler;

import com.cv4j.piccrawler.strategy.AutoIncrementStrategy;
import com.cv4j.piccrawler.strategy.NormalStrategy;
import com.cv4j.piccrawler.utils.Utils;
import com.safframework.tony.common.utils.IOUtils;
import com.safframework.tony.common.utils.Preconditions;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tony on 2017/10/27.
 */
public class DownloadManager {

    private final static int BUFFER_SIZE = 0x2000; // 8192
    private static AtomicInteger count = new AtomicInteger();
    private FileStrategy fileStrategy;

    private DownloadManager() {
    }

    public static DownloadManager get() {
        return DownloadManager.Holder.DOWNLOAD_MANAGER;
    }

    public FileStrategy getFileStrategy() {
        return fileStrategy;
    }

    public void setFileStrategy(FileStrategy fileStrategy) {
        this.fileStrategy = fileStrategy;
    }

    /**
     * 将response的响应流写入文件中
     * @param response
     * @param url

     * @return
     * @throws IOException
     */
    public File writeImageToFile(CloseableHttpResponse response, String url) throws IOException{

        if (response==null) return null;

        // 获取响应实体
        HttpEntity entity = response.getEntity();

        if (entity==null) return null;

        InputStream is = entity.getContent();

        // 包装成高效流
        BufferedInputStream bis = new BufferedInputStream(is);

        if (fileStrategy == null) {
            fileStrategy = new FileStrategy() {

                @Override
                public String filePath() {
                    return "images";
                }

                @Override
                public String picFormat() {
                    return "png";
                }

                @Override
                public FileGenType genType() {

                    return FileGenType.RANDOM;
                }
            };
        }

        String path = fileStrategy.filePath();

        String format = Utils.tryToGetPicFormat(url);
        // 优先使用url地址中图片的格式，如果不存在格式取fileStrategy的图片格式
        if (Preconditions.isBlank(format)) {
            format = fileStrategy.picFormat();
        }

        FileGenType fileGenType = fileStrategy.genType();

        File directory = null;
        // 写入本地文件
        if (Preconditions.isNotBlank(path)) {

            directory = new File(path);
            if (!directory.exists()) {

                if (path.contains("/")) {
                    directory.mkdirs();
                } else {
                    directory.mkdir();
                }

                if (!directory.exists() || !directory.isDirectory()) {

                    directory = Utils.mkDefaultDir(directory);
                }
            }
        } else {
            directory = Utils.mkDefaultDir(directory);
        }

        String fileName = null;
        switch (fileGenType) {

            case RANDOM:

                fileName = Utils.randomUUID();
                break;

            case AUTO_INCREMENT:

                if (fileStrategy instanceof AutoIncrementStrategy) {

                    if (count.get()< ((AutoIncrementStrategy) fileStrategy).start()) {
                        count.set(((AutoIncrementStrategy) fileStrategy).start());
                    }
                }

                count.incrementAndGet();
                fileName = String.valueOf(count.get());
                break;

            case NORMAL:

                fileName = ((NormalStrategy)fileStrategy).fileName();
                break;
        }

        if (Preconditions.isBlank(fileName)) fileName = "temp";

        File file = new File(directory, fileName + "." + format);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        byte[] byt = new byte[BUFFER_SIZE];
        Integer len = -1;
        while ((len = bis.read(byt)) != -1) {
            bos.write(byt, 0, len);
        }

        IOUtils.closeQuietly(bos);
        IOUtils.closeQuietly(bis);

        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                System.err.println("释放链接错误");
                e.printStackTrace();
            }
        }

        return file;
    }

    private static class Holder {
        private static final DownloadManager DOWNLOAD_MANAGER = new DownloadManager();
    }
}
