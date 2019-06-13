package com.lwy.smartupdate.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class FileUtils {

    public static String getFileName(String suffix) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",
                java.util.Locale.getDefault());
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(10000);
        return new StringBuffer().append(formatDate).append("_").append(random).append(".").append(suffix)
                .toString();
    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",
                java.util.Locale.getDefault());
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(10000);
        return new StringBuffer().append(formatDate).append("_").append(random)
                .toString();
    }

    /**
     * 将inputstream转成string
     *
     * @param is
     * @return结果string
     * @author Lwy
     * @date 2015-9-15 下午3:47:17
     */
    public static String is2String(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    /**
     * 判断文件是否存在
     */
    public static Boolean isFileExist(String sFileName) {
        File f = new File(sFileName);
        return f.exists();
    }

    /**
     * 获得yyyyMMddHHmmss_999的随机文件名
     */
    public static String getRandomFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(1000);
        return new StringBuffer().append(formatDate).append("_").append(random).toString();
    }

    /**
     * 删除单个文件
     */
    public static void deleteFile(String fileName) {
        File myFile = new File(fileName);
        if (myFile.exists()) {
            myFile.delete();
        }
    }

    /**
     * 删除单个文件
     */
    public static void deleteFile(String dir, String fileName) {
        File myFile = new File(dir, fileName);
        if (myFile.exists()) {
            myFile.delete();
        }
    }

    /**
     * 删除文件夹及文件夹里的所有文件
     */
    public static boolean delFolder(String folderPath) {
        boolean flag = false;
        try {
            deleteAllFiles(folderPath); // 删除文件夹里的所有文件
            File folder = new File(folderPath);
            folder.delete(); // 删除文件夹
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除指定路径里的所有文件(不包含该文件夹)
     */
    public static boolean deleteAllFiles(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp = null;
        if (tempList != null && tempList.length > 0) {
            for (int i = 0; i < tempList.length; i++) {
                temp = new File(path, tempList[i]);
                if (temp.isDirectory())
                    deleteAllFiles(temp.getAbsolutePath());
                else {
                    temp.delete();
                }

            }
        }
        return true;
    }

    /**
     * 删除目录下指定时间的所有文件
     *
     * @param day 几天前
     * @param dir 要删除的文件目录
     */
    public static void delFilesBeforeDate(int day, File dir) {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - day);
        Date borderDate = now.getTime();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                long time = f.lastModified();
                now.setTimeInMillis(time);
                if (now.getTime().before(borderDate)) {
                    f.delete();
                }
            }
        }
    }

    public static boolean string2file(String str, String filePath) {
        boolean flag = false;
        BufferedWriter bw = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(str);// 把整个json文件保存起来
            bw.flush();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public static String file2string(String filePath) {
        File file = new File(filePath);
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(file);
            br = new BufferedReader(fr);
            String str = null;
            StringWriter sw = new StringWriter();
            while ((str = br.readLine()) != null) {
                sw.write(str);
            }
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static boolean copyToFile(InputStream inputStream, File destFile) throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        OutputStream out = new FileOutputStream(destFile);
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.close();
        }
        return true;
    }

}
