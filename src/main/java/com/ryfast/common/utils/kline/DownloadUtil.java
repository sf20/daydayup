package com.ryfast.common.utils.kline;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {

    /**
     * 从网络Url中下载文件
     *
     * @param downloadUrl
     * @param fileSavePath
     * @param fileSaveName
     * @throws IOException
     */
    public static void downloadFromUrl(String downloadUrl, String fileSavePath, String fileSaveName)
            throws IOException {
        // step1：从网络流中拿数据
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 设置超时间为10秒
        conn.setConnectTimeout(10 * 1000);
        // 得到输入流
        InputStream inputStream = conn.getInputStream();
        // 获取字节数组
        byte[] getData = readInputStream(inputStream);

        // step2：往文件流中写数据
        // 文件保存位置
        File saveDir = new File(fileSavePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileSaveName);
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(getData);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } finally {
            bos.close();
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bos.toByteArray();
    }
}
