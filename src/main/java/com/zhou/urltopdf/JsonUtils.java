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

@Slf4j
public class JsonUtils {
    private static final Gson gson = new GsonBuilder().create();
    private static final String JSON_FILE_PATH = "src/main/微信公众号文章.json";
    private static final Type ARTICLE_LIST_TYPE = new TypeToken<List<Article>>(){}.getType();

    /**
     * 读取JSON文件并映射到List<Article>
     * @return List<Article> 文章列表
     */
    public static List<Article> readJsonFile() {
        try {
            File file = new File(JSON_FILE_PATH);
            if (file.exists()) {
                String json = new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH)));
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

    /**
     * 将List<Article>写入JSON文件
     */
    public static void writeJsonFile(List<Article> articles) {
        try (FileWriter writer = new FileWriter(JSON_FILE_PATH)) {
            gson.toJson(articles, ARTICLE_LIST_TYPE, writer);
            log.info("成功写入JSON文件，共 " + articles.size() + " 条记录");
        } catch (Exception e) {
            log.error("写入JSON文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
