package com.zhou.urltopdf;

public class StringUtils {
  /**
   * 清理字符串，使其可以作为Windows文件名使用
   * @param filename 原始文件名
   * @return 清理后的文件名
   */
  public static String sanitizeFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
      return "unnamed";
    }

    // Windows文件名中不允许的字符: \ / : * ? " < > |
    // 同时替换其他可能有问题的字符
    String sanitized = filename.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");

    // 限制文件名长度（Windows路径总长度限制为260个字符，但这里我们限制文件名为150个字符）
    if (sanitized.length() > 150) {
      sanitized = sanitized.substring(0, 150);
    }

    // 去除首尾空格
    sanitized = sanitized.trim();

    // 确保文件名不为空
    if (sanitized.isEmpty()) {
      sanitized = "unnamed";
    }

    return sanitized;
  }

}
