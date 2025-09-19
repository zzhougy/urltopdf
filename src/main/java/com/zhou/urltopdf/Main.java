package com.zhou.urltopdf;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Slf4j
public class Main {

  private static JTextArea logTextArea;
  // 添加日志行数计数器和限制
  private static int logLineCount = 0;
  private static final int MAX_LOG_LINES = 5000; // 设置最大日志行数

  public static void main(String[] args) {
    // 设置允许图形界面，解决java.awt.HeadlessException
    System.setProperty("java.awt.headless", "false");
    // 初始化 Swing 界面
    SwingUtilities.invokeLater(Main::createAndShowGUI);
    // 配置 Logback 将日志输出到 Swing 界面
    configureLogbackAppender();

    // 读取json文件，并且映射到List<Article>
    List<Article> articles = JsonUtils.readJsonFile();
    if (articles == null || articles.isEmpty()) {
      log.error("没有可用数据，请检查json文件");
      return;
    }
    List<Article> errorArticles = new ArrayList<>();
    // 未知异常
    boolean isUnknownException = false;


    try (Playwright playwright = Playwright.create()) {
      // 配置浏览器选项
      BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
              .setHeadless(true); // 设置为无头模式

      // 创建浏览器实例
      try (Browser browser = playwright.chromium().launch(launchOptions)) {
        // 创建浏览器上下文
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080); // 设置视口大小

        try (BrowserContext context = browser.newContext(contextOptions)) {
          // 创建新页面
          try (Page page = context.newPage()) {
            int i = 0;
            log.info("共" + articles.size() + "个");
            long startTime = System.currentTimeMillis(); // 记录开始时间
            for (Article article : articles) {

              try {
                String dateStr = "";
                try {
                  LocalDateTime dateTime = LocalDateTime.ofInstant(
                          java.time.Instant.ofEpochSecond(article.getCreate_time()),
                          ZoneId.systemDefault()
                  );
                  dateStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                } catch (Exception e) {
                  log.info("时间格式化错误。若无需时间字段，请忽略...");
                }
                String title = StringUtils.sanitizeFilename(article.getTitle());


                String desktopPath = FileUtils.getDesktopPath();

                String outputPath = desktopPath + File.separator + "urltopdf" + File.separator + dateStr + "_" + title + ".pdf";
                log.info("开始处理：" + outputPath);

                // 导航到目标URL
                page.navigate(article.getLink());

                // 等待页面完全加载
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                // 2. 滚动到底，触发懒加载
                page.evaluate("""
                async () => {
                    // 适配任何网站的通用滚动逻辑
                    // 1. 获取整个文档的高度
                    const body = document.body;
                    const html = document.documentElement;
                    
                    // 计算页面最大高度
                    const pageHeight = Math.max(
                        body.scrollHeight,
                        body.offsetHeight,
                        html.clientHeight,
                        html.scrollHeight,
                        html.offsetHeight
                    );
                    
                    // 2. 计算每次滚动步长：取屏幕高度 * 0.8，重叠一点更保险
                    const step = window.innerHeight * 0.8;
                    let current = 0;

                    // 3. 逐段向下滚动
                    while (current < pageHeight) {
                        window.scrollTo(0, current);
                        // 等待 200 ms 让浏览器触发懒加载
                        await new Promise(r => setTimeout(r, 200));
                        current += step;
                    }

                    // 4. 再回顶部
                    window.scrollTo(0, 0);
                    // 额外等待确保所有资源加载完成
                    await new Promise(r => setTimeout(r, 500));
                }
            """);

                // 获取页面宽度，用于设置PDF宽度
                Object pageWidthObj = page.evaluate("Math.max(document.body.scrollWidth, document.body.offsetWidth, document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth)");
                double pageWidth = Double.parseDouble(pageWidthObj.toString());
                // 设置PDF宽度为页面宽度，高度为自动
                String widthStr = pageWidth + "px";

                // 3. 等所有网络空闲（图片加载完）
//            page.waitForLoadState(LoadState.NETWORKIDLE);

                // 配置PDF选项
                Page.PdfOptions pdfOptions = new Page.PdfOptions()
                        .setPath(Paths.get(outputPath))
                        .setFormat("A4")
                        .setWidth(widthStr)
                        .setPrintBackground(true)
//                    .setMargin(new Margin(20, 20, 20, 20))
                        ; // 设置页边距

                // 生成PDF文件
                page.pdf(pdfOptions);

                log.info("PDF生成成功！保存路径: " + outputPath);

              } catch (Exception e) {
                log.error("出现异常，跳过。生成 {} 时出错。", article.getTitle(), e);
                errorArticles.add(article);
              }



              // 剩余个数
              i++;
              long currentTime = System.currentTimeMillis();
              long elapsedTime = currentTime - startTime; // 计算耗时
              long seconds = elapsedTime / 1000;
              long minutes = seconds / 60;
              seconds = seconds % 60;
              log.info("当前耗时: " + minutes + "分" + seconds + "秒");
              log.info("已完成" + i + "个");
              log.info("剩余" + (articles.size() - i) + "个");
              // 随机时间睡眠
              Random rand = new Random();
              int randomNum = rand.nextInt(3000 - 1000 + 1) + 1000; // [1000, 3000]
              log.info("随机等待" + randomNum + "ms");
              Thread.sleep(randomNum);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("生成PDF时出错: ", e);
      isUnknownException = true;
    }

    if (!isUnknownException) {
      log.info("====PDF生成完毕！");
      if ( !errorArticles.isEmpty()) {
        JsonUtils.writeJsonFile(errorArticles);
        log.error("====出现{}个失败，已记录到{}文件", errorArticles.size(), FileUtils.getDesktopPath() + FileUtils.ERROR_JSON_FILE_PATH);
      }

      extracted();

    } else {
      log.info("出现未知异常，已中断，请检查日志");
    }

  }

  private static void extracted() {
    // 弹出对话框让用户决定是否进行压缩处理
    SwingUtilities.invokeLater(() -> {
      int option = JOptionPane.showConfirmDialog(
          null,
          "PDF生成已完成，是否需要对" + FileUtils.getDesktopPath() + File.separator + "urltopdf/" + "中的pdf" + "进行压缩处理？",
          "PDF压缩选项",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE
      );

      if (option == JOptionPane.YES_OPTION) {
        // 在新线程中执行压缩操作，避免阻塞UI线程。否则无法实时看到log打印
        new Thread(() -> {
          List<String> compressErrorArticles = new ArrayList<>();
          log.info("用户选择进行PDF压缩处理");

          try {
            String folderPath = FileUtils.getDesktopPath() + File.separator + "urltopdf";
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

            // 创建compressed目录（如果不存在）
            String compressedDirPath = folderPath + File.separator + "compressed";
            File compressedDir = new File(compressedDirPath);
            if (!compressedDir.exists()) {
              if (!compressedDir.mkdirs()) {
                log.error("无法创建compressed目录: " + compressedDirPath);
                return;
              }
            }

            for (File file : files) {
              try {
                String compressedFilePath = compressedDirPath + File.separator + file.getName();
                File compressedPdf = new File(compressedFilePath);

                // 执行压缩
                PDFCompressor.compressPdf(file, compressedPdf);

                // 检查压缩是否成功
                if (compressedPdf.exists() && compressedPdf.length() > 0) {
                  log.info("PDF压缩成功: " + file.getPath());
                } else {
                  log.error("PDF压缩失败或生成的文件为空: " + file.getPath());
                  compressErrorArticles.add(file.getPath());
                }
              } catch (Exception e) {
                log.error("PDF压缩失败。", e);
                compressErrorArticles.add(file.getPath());
              }
            }
            log.info("批量压缩完成");
            if (!compressErrorArticles.isEmpty()) {
              String collect = String.join("\n", compressErrorArticles);
              log.error("====出现{}个pdf压缩失败。如下：\n [{}]", compressErrorArticles.size(), collect);
            }

          } catch (Exception e) {
            log.error("PDF压缩过程中发生错误，压缩中断。", e);
          }

        }).start();
      } else {
        log.info("用户选择不进行PDF压缩处理");
      }
    });
  }

  private static void createAndShowGUI() {
    JFrame frame = new JFrame("实时日志监控");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);

    logTextArea = new JTextArea();
    logTextArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(logTextArea);

    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    frame.setVisible(true);
  }

  private static void configureLogbackAppender() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(rootLogger.getLoggerContext());
    encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{20} - %msg%n");
    encoder.start();

    AppenderBase<ILoggingEvent> swingAppender = new AppenderBase<ILoggingEvent>() {
      @Override
      protected void append(ILoggingEvent eventObject) {
        String logMessage = encoder.getLayout().doLayout(eventObject);
        SwingUtilities.invokeLater(() -> {
          // 检查是否需要清空日志
          if (logLineCount >= MAX_LOG_LINES) {
            logTextArea.setText(""); // 清空日志
            logLineCount = 0;
            log.info("日志已清空，达到最大行数限制");
          }
          
          logTextArea.append(logMessage);
          logLineCount++; // 增加行数计数
        });
      }
    };
    swingAppender.setContext(rootLogger.getLoggerContext());
    swingAppender.start();

    rootLogger.addAppender(swingAppender);
  }

}