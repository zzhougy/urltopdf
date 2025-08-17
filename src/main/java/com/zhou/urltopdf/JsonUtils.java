package com.zhou.urltopdf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.zhou.urltopdf.FileUtils.JSON_FILE_PATH;

@Slf4j
public class JsonUtils {
    private static final Gson gson = new GsonBuilder().create();
    private static final Type ARTICLE_LIST_TYPE = new TypeToken<List<Article>>(){}.getType();

    /**
     * 读取JSON文件并映射到List<Article>
     * @return List<Article> 文章列表
     */
    public static List<Article> readJsonFile() {
        try {
          String desktopPath = FileUtils.getDesktopPath();

          File file = new File(desktopPath + JSON_FILE_PATH);
            if (file.exists()) {
                String json = new String(Files.readAllBytes(Paths.get(desktopPath + JSON_FILE_PATH)));
                return gson.fromJson(json, ARTICLE_LIST_TYPE);
            } else {
                log.info("JSON文件不存在，返回空列表");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("读取JSON文件时出错: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
