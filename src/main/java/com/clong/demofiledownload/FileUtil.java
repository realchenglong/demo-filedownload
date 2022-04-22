package com.clong.demofiledownload;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    /***
     * 下载一个文件
     */
    public static void outputOneFileByStream(HttpServletRequest request, HttpServletResponse response, String... args) {
        String filepath = "",downloadName = "";
        if(!StringUtils.isEmpty(args[0]))
        {
            filepath = args[0];
        }else{
            return;
        }

        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            return;
        }else if(!StringUtils.isEmpty(args[1]))
        {
            downloadName = args[1];
        }else{
            downloadName = filepath.substring(filepath.lastIndexOf(File.pathSeparatorChar),filepath.length());
        }

        //响应头的设置
        response.reset();
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");

        //设置压缩包的名字
        //解决不同浏览器压缩包名字含有中文时乱码的问题
        String agent = request.getHeader("USER-AGENT");
        try {
            if (agent.contains("MSIE") || agent.contains("Trident")) {
                downloadName = java.net.URLEncoder.encode(downloadName, "UTF-8");
            } else {
                downloadName = new String(downloadName.getBytes("UTF-8"), "ISO-8859-1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment;fileName=\"" + downloadName + "\"");

        //设置压缩流：直接写入response，实现边压缩边下载
        ZipOutputStream zipos = null;
        try {
            zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipos.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法
        } catch (Exception e) {
            e.printStackTrace();
        }
        File[] files = file.listFiles();
        //循环将文件写入压缩流
        DataOutputStream os = null;
        for (int i = 0; i < files.length; i++) {//要下载的文件个数

            File tempfile = files[i];
            try {
                //添加ZipEntry，并ZipEntry中写入文件流
                //这里，加上i是防止要下载的文件有重名的导致下载失败
                zipos.putNextEntry(new ZipEntry(tempfile.getName()));
                os = new DataOutputStream(zipos);
                InputStream is = new FileInputStream(tempfile);
                byte[] b = new byte[100];
                int length = 0;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                zipos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //关闭流
        try {
            os.flush();
            os.close();
            zipos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void download(HttpServletResponse response,String path) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = response.getOutputStream();
        try {

            File file = new File(path);
            //使用bufferedInputStream 缓存流的方式来获取下载文件，不然大文件会出现内存溢出的情况
            inputStream = new FileInputStream(file);
            //这里也很关键每次读取的大小为5M 不一次性读取完
            byte[] buffer = new byte[1024 * 1024 * 5];// 5MB
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }catch (Exception e){

        }finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }

    }
}
