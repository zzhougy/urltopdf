package com.zhou.urltopdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class PDFCompressor {

  // 压缩等级
  public enum CompressionLevel {
    LOW,             // 低压缩率，保持高质量
    MEDIUM,          // 中等压缩率，平衡质量和大小
    HIGH,            // 高压缩率，牺牲部分质量
    EXTREME,         // 极限压缩率，可能明显影响质量
    ULTRA_EXTREME,    // 超极限压缩率，最大化减小文件大小，可能显著影响质量

    CUSTOM    // 自定义压缩
  }

  /**
   * 压缩PDF文件
   *
   * @param inputFile  输入PDF文件
   * @param outputFile 输出PDF文件
   * @param level      压缩等级
   * @throws IOException IO异常
   */
  public static void compressPdf(File inputFile, File outputFile, CompressionLevel level) throws IOException {
    log.info("开始压缩PDF文件：" + inputFile.getName());
    long startTime = System.currentTimeMillis();
    long originalSize = inputFile.length();

    try (PDDocument document = PDDocument.load(inputFile)) {
      if (CompressionLevel.CUSTOM != level) {
        // 1. 移除不必要的元数据
        removeUnnecessaryMetadata(document);

        // 2. 优化图片（最有效的压缩策略）
        optimizeImages(document, level);

        // 3. 优化字体
        optimizeFonts(document, level);

        // 4. 优化内容流
        optimizeContentStreams(document);

        // 5. 合并重复对象
        optimizeDuplicateObjects(document);

        // 6. 优化页面内容
        optimizePageContents(document);

        // 7. 移除表单
        removeForms(document);

        // 8. 对于超极限压缩，尝试额外的优化
        if (level == CompressionLevel.ULTRA_EXTREME) {
          ultraOptimizePdf(document);
        }
      } else {
        // 优化图片（最有效的压缩策略）
        optimizeImages(document, level);
      }

      // 9. 保存文档时强制压缩
      document.save(outputFile);
    }

    long endTime = System.currentTimeMillis();
    long compressedSize = outputFile.length();
    double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

    log.info("PDF文件压缩完成：" + outputFile.getName());
    log.info(String.format("原始大小: %.2f MB, 压缩后大小: %.2f MB, 压缩率: %.2f%%, 耗时: %d 秒",
            originalSize / 1024.0 / 1024.0,
            compressedSize / 1024.0 / 1024.0,
            compressionRatio,
            (endTime - startTime) / 1000));
  }

  /**
   * 压缩PDF文件，使用中等压缩等级
   *
   * @param inputFile  输入PDF文件
   * @param outputFile 输出PDF文件
   * @throws IOException IO异常
   */
  public static void compressPdf(File inputFile, File outputFile) throws IOException {
    compressPdf(inputFile, outputFile, CompressionLevel.CUSTOM);
  }

  /**
   * 移除不必要的元数据
   */
  private static void removeUnnecessaryMetadata(PDDocument document) {
    try {
      // 移除XMP元数据
      PDMetadata metadata = document.getDocumentCatalog().getMetadata();
      if (metadata != null) {
        document.getDocumentCatalog().setMetadata(null);
      }

      // 移除生产者信息
      document.getDocumentInformation().setProducer(null);
      document.getDocumentInformation().setCreator(null);
      document.getDocumentInformation().setKeywords(null);

      // 保留标题、主题和作者，这些可能对用户有用
    } catch (Exception e) {
      log.error("移除元数据时出错: ", e);
    }
  }

  /**
   * 优化图片
   */
  private static void optimizeImages(PDDocument document, CompressionLevel level) {
    try {
      float quality = 0.8f; // 默认质量
      int maxDPI = 300;     // 默认最大DPI
      boolean convertToGrayscale = false; // 是否转换为灰度
      boolean downsampleAllImages = false; // 是否下采样所有图片（无论原始DPI）
      boolean useJPEG2000 = false; // 是否使用JPEG 2000格式
      boolean forceWebOptimizedFormat = false; // 是否强制使用网络优化格式
      boolean binarizeMonochrome = false; // 是否二值化单色图像

      // 根据压缩等级调整参数
      switch (level) {
        case LOW:
          quality = 0.9f;
          maxDPI = 300;
          break;
        case MEDIUM:
          quality = 0.7f;
          maxDPI = 200;
          break;
        case HIGH:
          quality = 0.5f;
          maxDPI = 150;
          convertToGrayscale = true; // 高压缩时转换为灰度
          break;
        case EXTREME:
          quality = 0.3f;
          maxDPI = 100;
          convertToGrayscale = true;
          downsampleAllImages = true;
          binarizeMonochrome = true;
          break;
        case ULTRA_EXTREME:
          quality = 0.15f; // 超极限压缩的图片质量
          maxDPI = 72;     // PDF默认DPI
          convertToGrayscale = true;
          downsampleAllImages = true;
          forceWebOptimizedFormat = true;
          binarizeMonochrome = true;
          useJPEG2000 = true;
          break;
        case CUSTOM:
          /**
           * 这种能极限压缩大小，但是包含文字的图片可能看不清
           * quality = 0.15f; // 超极限压缩的图片质量
           * maxDPI = 72;     // PDF默认DPI
           * downsampleAllImages = true;
           * forceWebOptimizedFormat = true;
           * binarizeMonochrome = true;
           */

          quality = 0.5f;
          maxDPI = 150;
          downsampleAllImages = true;
          forceWebOptimizedFormat = true;
          binarizeMonochrome = true;
          break;
      }

      // 遍历所有页面
      for (PDPage page : document.getPages()) {
        PDResources resources = page.getResources();
        Iterable<COSName> xObjectNames = resources.getXObjectNames();

        if (xObjectNames != null) {
          for (COSName name : xObjectNames) {
            if (resources.isImageXObject(name)) {
              PDImageXObject image = (PDImageXObject) resources.getXObject(name);

              // 压缩策略1: 获取原始图像
              BufferedImage bufferedImage = image.getImage();

              // 压缩策略2: 二值化单色图像（对于ULTRA_EXTREME和EXTREME）
              if (binarizeMonochrome && isMonochromeImage(bufferedImage)) {
                bufferedImage = binarizeImage(bufferedImage);
              }

              // 压缩策略3: 转换为灰度图像（对于高压缩等级）
              if (convertToGrayscale && bufferedImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
                bufferedImage = convertToGrayscale(bufferedImage);
              }

              // 计算当前DPI（假设页面大小为A4）
              float pageWidth = page.getMediaBox().getWidth();
              float dpi = (bufferedImage.getWidth() / pageWidth) * 72; // 72是PDF默认DPI

              // 压缩策略4: 降低图片分辨率
              boolean needsResizing = downsampleAllImages || dpi > maxDPI;

              if (needsResizing) {
                // 创建一个新的低分辨率图像
                int targetDPI = downsampleAllImages ? maxDPI : Math.min((int) dpi, maxDPI);
                int newWidth = (int) (bufferedImage.getWidth() * targetDPI / dpi);
                int newHeight = (int) (bufferedImage.getHeight() * targetDPI / dpi);

                // 为超极限压缩使用更快速的缩放方法以提高压缩速度
                int scalingMethod = level == CompressionLevel.ULTRA_EXTREME ?
                        BufferedImage.SCALE_FAST : BufferedImage.SCALE_SMOOTH;

                BufferedImage resizedImage = new BufferedImage(
                        newWidth, newHeight,
                        bufferedImage.getType() == BufferedImage.TYPE_BYTE_GRAY ?
                                BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB);

                resizedImage.createGraphics().drawImage(
                        bufferedImage.getScaledInstance(newWidth, newHeight, scalingMethod),
                        0, 0, null);

                bufferedImage = resizedImage;
              }

              // 压缩策略5: 根据图像类型和压缩等级选择最佳输出格式
              String outputFormat = "png";
              if (forceWebOptimizedFormat) {
                // 对于网络优化，JPEG通常比PNG更高效
                outputFormat = "jpg";
              }

              // 替换原始图像
              PDImageXObject newImage = PDImageXObject.createFromByteArray(
                      document, imageToByteArrayWithQuality(bufferedImage, outputFormat, quality), "image");
              resources.put(name, newImage);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("优化图片时出错: ", e);
    }
  }

  /**
   * 优化字体
   */
  private static void optimizeFonts(PDDocument document, CompressionLevel level) {
    try {
      // 对于ULTRA_EXTREME和EXTREME等级，我们可以尝试更多字体优化
      if (level == CompressionLevel.ULTRA_EXTREME || level == CompressionLevel.EXTREME) {
        // 遍历所有页面查找并移除未使用的字体
        removeUnusedFonts(document);
      }
    } catch (Exception e) {
      log.error("优化字体时出错: ", e);
    }
  }

  /**
   * 移除未使用的字体
   */
  private static void removeUnusedFonts(PDDocument document) {
    try {
      Set<String> usedFontNames = new HashSet<>();

      // 创建一个自定义的PDFStreamEngine来跟踪使用的字体
      PDFStreamEngine engine = new PDFStreamEngine() {
        {
          addOperator(new Concatenate());
          addOperator(new Restore());
          addOperator(new Save());
          addOperator(new SetGraphicsStateParameters());
          addOperator(new SetMatrix());
          addOperator(new BeginText());
          addOperator(new EndText());
          addOperator(new ShowText());
          addOperator(new ShowTextAdjusted());
          addOperator(new MoveText());
          addOperator(new MoveTextSetLeading());
          addOperator(new SetCharSpacing());
          addOperator(new SetFontAndSize() {
            @Override
            public void process(Operator operator, List<COSBase> arguments) throws IOException {
              super.process(operator, arguments);
              if (arguments.size() >= 2 && arguments.get(0) instanceof COSName) {
                COSName fontName = (COSName) arguments.get(0);
                usedFontNames.add(fontName.getName());
              }
            }
          });
          addOperator(new SetTextHorizontalScaling());
          addOperator(new SetTextLeading());
          addOperator(new SetTextRenderingMode());
          addOperator(new SetTextRise());
          addOperator(new SetWordSpacing());
        }
      };

      // 处理所有页面以识别使用的字体
      for (PDPage page : document.getPages()) {
        try {
          engine.processPage(page);
        } catch (Exception e) {
          // 忽略页面处理错误，继续处理其他页面
          log.debug("处理页面字体时出错: ", e);
        }
      }

      // 这里可以添加移除未使用字体的代码
      // 注意：完整的字体移除需要更复杂的实现，因为需要考虑字体的引用关系
    } catch (Exception e) {
      log.error("移除未使用字体时出错: ", e);
    }
  }

  /**
   * 优化内容流
   */
  private static void optimizeContentStreams(PDDocument document) {
    try {
      // PDFBox会自动优化内容流，这里我们可以添加额外的优化
      document.saveIncremental(new java.io.ByteArrayOutputStream());
    } catch (Exception e) {
      log.error("优化内容流时出错: ", e);
    }
  }

  /**
   * 优化重复对象
   */
  private static void optimizeDuplicateObjects(PDDocument document) {
    try {
      // PDFBox会在保存时自动合并一些重复对象
      // 这里可以添加更复杂的重复对象检测和合并
      document.saveIncremental(new java.io.ByteArrayOutputStream());
    } catch (Exception e) {
      log.error("优化重复对象时出错: ", e);
    }
  }

  /**
   * 超极限优化PDF（仅用于ULTRA_EXTREME等级）
   */
  private static void ultraOptimizePdf(PDDocument document) {
    try {
      // 1. 移除所有注释
      removeAnnotations(document);

      // 2. 移除所有交互元素
      removeInteractiveElements(document);

      // 3. 移除所有书签
      removeBookmarks(document);

      // 4. 移除所有可选内容组
      removeOptionalContentGroups(document);

      // 5. 尝试重新保存为更紧凑的格式
      document.saveIncremental(new java.io.ByteArrayOutputStream());
    } catch (Exception e) {
      log.error("超极限优化PDF时出错: ", e);
    }
  }

  /**
   * 移除PDF中的所有注释
   */
  private static void removeAnnotations(PDDocument document) {
    try {
      for (PDPage page : document.getPages()) {
        page.setAnnotations(null);
      }
    } catch (Exception e) {
      log.error("移除注释时出错: ", e);
    }
  }

  /**
   * 移除PDF中的所有交互元素
   */
  private static void removeInteractiveElements(PDDocument document) {
    try {
      // 已经在removeForms中移除了表单，这里可以添加其他交互元素的移除
    } catch (Exception e) {
      log.error("移除交互元素时出错: ", e);
    }
  }

  /**
   * 移除PDF中的所有书签
   */
  private static void removeBookmarks(PDDocument document) {
    try {
      PDDocumentCatalog catalog = document.getDocumentCatalog();
      if (catalog.getDocumentOutline() != null) {
        catalog.setDocumentOutline(null);
      }
    } catch (Exception e) {
      log.error("移除书签时出错: ", e);
    }
  }

  /**
   * 移除PDF中的所有可选内容组
   */
  private static void removeOptionalContentGroups(PDDocument document) {
    try {
      PDDocumentCatalog catalog = document.getDocumentCatalog();
      if (catalog.getOCProperties() != null) {
        catalog.setOCProperties(null);
      }
    } catch (Exception e) {
      log.error("移除可选内容组时出错: ", e);
    }
  }

  /**
   * 优化页面内容
   */
  private static void optimizePageContents(PDDocument document) {
    try {
      // 这里可以添加页面内容的优化，如移除空白页面、合并相似页面等
    } catch (Exception e) {
      log.error("优化页面内容时出错: ", e);
    }
  }

  /**
   * 移除非必要的表单
   */
  private static void removeForms(PDDocument document) {
    try {
      PDDocumentCatalog catalog = document.getDocumentCatalog();
      PDAcroForm acroForm = catalog.getAcroForm();
      if (acroForm != null && acroForm.getFields().isEmpty()) {
        catalog.setAcroForm(null);
      }
    } catch (Exception e) {
      log.error("移除表单时出错: ", e);
    }
  }

  /**
   * 将BufferedImage转换为byte数组
   */
  private static byte[] imageToByteArray(BufferedImage image, String format) {
    try {
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      ImageIO.write(image, format, baos);
      return baos.toByteArray();
    } catch (IOException e) {
      log.error("转换图像时出错: ", e);
      return null;
    }
  }

  /**
   * 将BufferedImage以指定质量转换为byte数组
   */
  private static byte[] imageToByteArrayWithQuality(BufferedImage image, String format, float quality) {
    try {
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

      // 获取图像写入器
      javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName(format).next();
      javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();

      // 设置压缩质量（仅对支持压缩的格式有效）
      if (param.canWriteCompressed()) {
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
      }

      writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
      writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
      writer.dispose();

      return baos.toByteArray();
    } catch (Exception e) {
      log.error("转换图像时出错: ", e);
      // 如果设置质量失败，回退到普通转换
      return imageToByteArray(image, format);
    }
  }

  /**
   * 检查图像是否为单色图像
   */
  private static boolean isMonochromeImage(BufferedImage image) {
    // 检查图像类型是否为灰度
    if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      return true;
    }

    // 对于其他类型，采样检查是否只有黑白颜色
    int sampleSize = Math.min(image.getWidth(), image.getHeight()) / 10;
    if (sampleSize < 1) sampleSize = 1;

    for (int x = 0; x < image.getWidth(); x += sampleSize) {
      for (int y = 0; y < image.getHeight(); y += sampleSize) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 如果不是灰度颜色
        if (r != g || g != b) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * 将图像转换为灰度
   */
  private static BufferedImage convertToGrayscale(BufferedImage image) {
    BufferedImage grayImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 计算灰度值（使用 luminance 公式）
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int grayRGB = (gray << 16) | (gray << 8) | gray;

        grayImage.setRGB(x, y, grayRGB);
      }
    }

    return grayImage;
  }

  /**
   * 二值化图像（转换为黑白）
   */
  private static BufferedImage binarizeImage(BufferedImage image) {
    BufferedImage binaryImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

    // 使用Otsu阈值法计算最佳阈值
    int threshold = calculateOtsuThreshold(image);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 计算亮度
        int brightness = (int) (0.299 * r + 0.587 * g + 0.114 * b);

        // 根据阈值设置黑白
        int binaryColor = brightness > threshold ? 0xFFFFFF : 0x000000;
        binaryImage.setRGB(x, y, binaryColor);
      }
    }

    return binaryImage;
  }

  /**
   * 使用Otsu算法计算图像阈值
   */
  private static int calculateOtsuThreshold(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    int[] histogram = new int[256];

    // 计算直方图
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 计算亮度
        int brightness = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        histogram[brightness]++;
      }
    }

    // Otsu算法计算最佳阈值
    int total = width * height;
    double sum = 0;
    for (int i = 0; i < 256; i++) sum += i * histogram[i];

    double sumB = 0;
    int wB = 0;
    int wF = 0;

    double varMax = 0;
    int threshold = 0;

    for (int i = 0; i < 256; i++) {
      wB += histogram[i];
      if (wB == 0) continue;

      wF = total - wB;
      if (wF == 0) break;

      sumB += (double) (i * histogram[i]);
      double mB = sumB / wB;
      double mF = (sum - sumB) / wF;

      double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

      if (varBetween > varMax) {
        varMax = varBetween;
        threshold = i;
      }
    }

    return threshold;
  }

  /**
   * 批量压缩文件夹中的所有PDF文件
   *
   * @param folderPath 文件夹路径
   * @param level      压缩等级
   */
  public static void batchCompressPdfs(String folderPath, CompressionLevel level) {
    File folder = new File(folderPath);
    if (!folder.exists() || !folder.isDirectory()) {
      log.error("文件夹不存在: " + folderPath);
      return;
    }

    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
    if (files == null || files.length == 0) {
      log.info("文件夹中没有PDF文件: " + folderPath);
      return;
    }

    log.info("开始批量压缩PDF文件，共" + files.length + "个文件");

    for (File file : files) {
      try {
        String outputPath = file.getAbsolutePath().replace(".pdf", "_compressed.pdf");
        compressPdf(file, new File(outputPath), level);
      } catch (Exception e) {
        log.error("压缩文件{}时出错: ", file.getName(), e);
      }
    }

    log.info("批量压缩完成");
  }

  /**
   * 批量压缩文件夹中的所有PDF文件，使用中等压缩等级
   *
   * @param folderPath 文件夹路径
   */
  public static void batchCompressPdfs(String folderPath) {
    batchCompressPdfs(folderPath, CompressionLevel.MEDIUM);
  }
}