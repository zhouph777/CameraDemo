package com.util.camerademo;

import java.io.File;
import java.io.IOException;

/**
 * 保存文件类
 * create by zhouph777@gmail.com
 */
public class SaveFile {

    public static File createFile(String filePath,String fileName){
        File folder = new File(filePath);
        if (!folder.exists()){
            folder.mkdir();
        }
        File file = new File(filePath+fileName);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


}
