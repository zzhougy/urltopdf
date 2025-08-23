package com.zhou.urltopdf;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileUtils {
  public static final String JSON_FILE_PATH = "/urltopdf/微信公众号文章.json";
  public static final String ERROR_JSON_FILE_PATH = "/urltopdf/微信公众号文章_error.json";

  public static String getDesktopPath() {
    String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    File desktop = new File(desktopPath);

    // 检查路径是否存在
    if (desktop.exists() && desktop.isDirectory()) {
      log.info("桌面路径: " + desktop.getAbsolutePath());
    } else {
      log.error("未找到桌面路径: " + desktopPath);
      throw new RuntimeException("未找到桌面路径: " + desktopPath);
    }

    return desktopPath;
  }



}
