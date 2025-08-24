package com.zhou.urltopdf;

import java.io.File;
import java.io.IOException;

/**
 * PDFCompressor的测试类，用于单独测试PDF压缩功能
 */
public class PDFCompressorTest {

  /**
   * 主方法，提供简单的命令行界面来测试PDF压缩功能
   * <p>
   * 可以通过以下方式使用：
   * 1. 不传参数：使用默认的测试文件路径
   * 2. 传入一个PDF文件路径：压缩指定的PDF文件
   * 3. 传入一个文件夹路径：批量压缩文件夹中的所有PDF文件
   * 4. 传入参数 "help"：显示帮助信息
   */
  public static void main(String[] args) {
    try {
      // 处理命令行参数
      if (args.length > 0) {
        String path = args[0];

        if ("help".equalsIgnoreCase(path)) {
          showHelp();
          return;
        }

        File target = new File(path);
        if (target.exists()) {
          if (target.isFile() && path.toLowerCase().endsWith(".pdf")) {
            // 压缩单个PDF文件
            compressSingleFile(target);
          } else if (target.isDirectory()) {
            // 批量压缩文件夹中的PDF文件
            batchCompressFolder(target);
          } else {
            System.out.println("指定的路径既不是PDF文件也不是文件夹: " + path);
            showHelp();
          }
        } else {
          System.out.println("指定的路径不存在: " + path);
          showHelp();
        }
      } else {
        // 没有传入参数，使用默认测试路径
        System.out.println("未传入参数，使用默认测试路径");

        // 尝试在桌面上的urltopdf文件夹中查找PDF文件
        String desktopPath = FileUtils.getDesktopPath();
        File testFolder = new File(desktopPath + File.separator + "urltopdf");

        if (testFolder.exists() && testFolder.isDirectory()) {
          File[] pdfFiles = testFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

          if (pdfFiles != null && pdfFiles.length > 0) {
            // 选择第一个PDF文件进行测试
            System.out.println("找到 " + pdfFiles.length + " 个PDF文件，使用第一个进行测试");
            compressSingleFile(pdfFiles[0]);
          } else {
            System.out.println("默认路径中没有找到PDF文件: " + testFolder.getAbsolutePath());
            showHelp();
          }
        } else {
          System.out.println("默认测试文件夹不存在: " + testFolder.getAbsolutePath());
          showHelp();
        }
      }
    } catch (Exception e) {
      System.err.println("测试过程中发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 压缩单个PDF文件，演示不同压缩等级的效果
   */
  private static void compressSingleFile(File inputFile) throws IOException {
    System.out.println("开始测试单个PDF文件压缩: " + inputFile.getName());

    // 测试不同压缩等级
    testDifferentCompressionLevels(inputFile);

    // 测试默认压缩方法（中等压缩等级）
    String defaultOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_default_compressed.pdf");
    File defaultOutputFile = new File(defaultOutputPath);

    System.out.println("\n测试默认压缩方法（中等压缩等级）:");
    PDFCompressor.compressPdf(inputFile, defaultOutputFile);

    System.out.println("\n单个PDF文件压缩测试完成！");
  }

  /**
   * 测试不同压缩等级的效果
   */
  private static void testDifferentCompressionLevels(File inputFile) throws IOException {
    System.out.println("\n测试不同压缩等级的效果:");

    // 低压缩率，保持高质量
    String lowOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_low_compressed.pdf");
    File lowOutputFile = new File(lowOutputPath);
    System.out.println("\n1. 低压缩率，保持高质量:");
    PDFCompressor.compressPdf(inputFile, lowOutputFile, PDFCompressor.CompressionLevel.LOW);

    // 中等压缩率，平衡质量和大小
    String mediumOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_medium_compressed.pdf");
    File mediumOutputFile = new File(mediumOutputPath);
    System.out.println("\n2. 中等压缩率，平衡质量和大小:");
    PDFCompressor.compressPdf(inputFile, mediumOutputFile, PDFCompressor.CompressionLevel.MEDIUM);

    // 高压缩率，牺牲部分质量
    String highOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_high_compressed.pdf");
    File highOutputFile = new File(highOutputPath);
    System.out.println("\n3. 高压缩率，牺牲部分质量:");
    PDFCompressor.compressPdf(inputFile, highOutputFile, PDFCompressor.CompressionLevel.HIGH);

    // 极限压缩率，可能明显影响质量
    String extremeOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_extreme_compressed.pdf");
    File extremeOutputFile = new File(extremeOutputPath);
    System.out.println("\n4. 极限压缩率，可能明显影响质量:");
    PDFCompressor.compressPdf(inputFile, extremeOutputFile, PDFCompressor.CompressionLevel.EXTREME);

    // 超极限压缩率，最大化减小文件大小，可能显著影响质量
    String ultraExtremeOutputPath = inputFile.getAbsolutePath().replace(".pdf", "_ultra_extreme_compressed.pdf");
    File ultraExtremeOutputFile = new File(ultraExtremeOutputPath);
    System.out.println("\n5. 超极限压缩率，最大化减小文件大小，可能显著影响质量:");
    PDFCompressor.compressPdf(inputFile, ultraExtremeOutputFile, PDFCompressor.CompressionLevel.ULTRA_EXTREME);
  }

  /**
   * 批量压缩文件夹中的所有PDF文件
   */
  private static void batchCompressFolder(File folder) {
    System.out.println("开始批量压缩文件夹中的PDF文件: " + folder.getAbsolutePath());

    // 测试默认的批量压缩方法（中等压缩等级）
    System.out.println("\n使用默认中等压缩等级批量压缩:");
    PDFCompressor.batchCompressPdfs(folder.getAbsolutePath());

    // 也可以指定压缩等级，例如：
    // PDFCompressor.batchCompressPdfs(folder.getAbsolutePath(), PDFCompressor.CompressionLevel.HIGH);

    System.out.println("\n批量压缩测试完成！");
  }

  /**
   * 显示帮助信息
   */
  private static void showHelp() {
    System.out.println("\nPDFCompressor测试工具使用帮助:");
    System.out.println("----------------------------------------");
    System.out.println("用法:");
    System.out.println("  java -cp <classpath> com.zhou.urltopdf.PDFCompressorTest [选项]");
    System.out.println("\n选项:");
    System.out.println("  [PDF文件路径]    : 压缩指定的PDF文件");
    System.out.println("  [文件夹路径]     : 批量压缩文件夹中的所有PDF文件");
    System.out.println("  help             : 显示此帮助信息");
    System.out.println("  无参数           : 使用默认测试路径");
    System.out.println("\n功能说明:");
    System.out.println("  1. 支持多种压缩等级：低(保持高质量)、中(平衡)、高(牺牲部分质量)、极限(可能明显影响质量)、超极限(最大化减小文件大小)");
    System.out.println("  2. 压缩策略包括：图片优化、元数据移除、内容流优化等");
    System.out.println("  3. 提供详细的压缩统计信息：原始大小、压缩后大小、压缩率、耗时");
    System.out.println("----------------------------------------");
  }
}