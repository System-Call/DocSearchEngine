package wiki.win.common;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @program: JavaApiSearchEngine
 * @description: 制作索引数据结构
 * @author: xiedy
 * @create: 2023-02-03 16:03
 **/
public class Parser {


    private Index index = new Index();


    private static File rootPath;

    //初始化
    static {
        try {
            rootPath = new File("").getCanonicalFile();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String BASE_PATH = "C:\\Users\\Administrator\\Desktop\\resource\\jdk-8u361-docs-all\\docs\\api";

    private static final String BASE_URL = "https://docs.oracle.com/javase/8/docs/api";

    public void run() {

        List<File> fileList = new ArrayList<>();
        enumFile(BASE_PATH, fileList);

        //枚举所有文件 解析并建立索引 属于耗时操作 开启多线程
        //ThreadFactory threadFactory = new ThreadFactory();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(fileList.size());
        try {
            for (File file : fileList) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        //解析和建立索引
                        parseHtml(file);
                        //保证所有的索引制作完再保存索引
                        countDownLatch.countDown();
                    }
                });

            }
            countDownLatch.await();
            executorService.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //保存索引
        index.save();
    }

    private void enumFile(String path, List<File> fileList) {

        File rootPath = new File(path);

        //listFiles()类似于Linux的ls把当前目录中包含的文件名获取到
        //使用listFiles只可以看见一级目录，想看到子目录需要递归操作
        File[] files = rootPath.listFiles();

        for (File f : files) {
            //根据当前的file的类型，觉得是否递归
            //如果file是普通文件就把file加入到listFile里面
            //如果file是一个目录 就递归调用enumFile这个方法，来进一步获取子目录的内容
            if (f.isDirectory()) {
                enumFile(f.getAbsolutePath(), fileList);
            } else {
                if (f.getAbsolutePath().endsWith("html")) {
                    fileList.add(f);
                }
            }
        }

    }

    private void parseHtml(File file) {
        // 1. 解析出HTML标题
        String title = parseTitle(file);
        // 2. 解析出HTML对应的文章
        String url = parseUrl(file);
        // 3. 解析出HTML对应的正文（有正文才有后续的描述）
        String content = parseContent(file);
        //构建索引
        index.addDoc(title,url,content);
    }


    private String parseContent(File f) {

        String content = null;
        try (FileReader fr = new FileReader(f)) {
            //开关
            boolean isCopy = true;

            StringBuffer sb = new StringBuffer();
            //复制文档内容 <>之内的不复制
            while (true) {
                //int类型 读到最后返回-1
                int ret = fr.read();
                if (ret == -1) {
                    break;
                }
                char c = (char) ret;
                //
                if (isCopy) {
                    if (c == '<') {
                        //<>中的内容不copy,关闭开关,
                        isCopy = false;
                        continue;
                    }

                    if (c == '\n' || c == '\r') {
                        //换行变空格
                        c = ' ';
                    }
                    sb.append(c);
                } else {
                    if (c == '>') {
                        isCopy = true;
                    }
                }
            }
            content = new String(sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private String parseUrl(File f) {

        String url = null;
        String path = f.getAbsolutePath();
        path = BASE_URL + path.substring(BASE_PATH.length());
        return path;

    }

    private String parseTitle(File f) {

        String title = null;
        String extension = FilenameUtils.getExtension(f.getName());
        String fName = f.getName();
        title = fName.substring(0, fName.lastIndexOf(extension) - 1);
        return title;
    }
}
