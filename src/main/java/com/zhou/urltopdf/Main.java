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
import java.util.List;
import java.util.Random;


@Slf4j
public class Main {

  private static JTextArea logTextArea;

  public static void main(String[] args) {
    // 设置允许图形界面，解决java.awt.HeadlessException
    System.setProperty("java.awt.headless", "false");
    // 初始化 Swing 界面
    SwingUtilities.invokeLater(Main::createAndShowGUI);
    // 配置 Logback 将日志输出到 Swing 界面
    configureLogbackAppender();

    // 读取json文件，并且映射到List<Article>
    List<Article> articles = JsonUtils.readJsonFile();
    List<Article> errorarticles = JsonUtils.readJsonFile();

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
            for (Article article : articles) {

              try {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(article.getCreate_time()),
                        ZoneId.systemDefault()
                );
                String dateStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String title = StringUtils.sanitizeFilename(article.getTitle());


                String desktopPath = FileUtils.getDesktopPath();

                String outputPath = desktopPath + File.separator + "urltopdf" + File.separator + dateStr + "_" + title + ".pdf";
                log.info("开始处理文章：" + outputPath);

                // 导航到目标URL
                page.navigate(article.getLink());

                // 等待页面完全加载
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                // 2. 滚动到底，触发懒加载
                page.evaluate("""
                async () => {
                    // 1. 先拿到正文容器（微信文章通常是 id="page-content" 或 js_content）
                    const article = document.querySelector('#page-content, #js_content, [rich_content]');
                    if (!article) return;
                    
                    // 2. 计算每次滚动步长：取屏幕高度 * 0.8，重叠一点更保险
                    const step = window.innerHeight * 0.8;
                    let current = 0;

                    // 3. 逐段向下滚动
                    while (current < article.scrollHeight) {
                        window.scrollTo(0, current);
                        // 等待 200 ms 让浏览器触发懒加载
                        await new Promise(r => setTimeout(r, 200));
                        current += step;
                    }

                    // 4. 再回顶部
                    window.scrollTo(0, 0);
                }
            """);

                // 3. 等所有网络空闲（图片加载完）
//            page.waitForLoadState(LoadState.NETWORKIDLE);

                // 配置PDF选项
                Page.PdfOptions pdfOptions = new Page.PdfOptions()
                        .setPath(Paths.get(outputPath))
                        .setFormat("A4")
                        .setPrintBackground(true)
//                    .setMargin(new Margin(20, 20, 20, 20))
                        ; // 设置页边距

                // 生成PDF文件
                page.pdf(pdfOptions);

                log.info("PDF生成成功！保存路径: " + outputPath);
              } catch (Exception e) {
                log.error("出现异常，跳过。生成 {} 时出错: {}", article.getTitle(), e.getMessage());
                e.printStackTrace();
                errorarticles.add(article);
              }



              // 剩余个数
              i++;
              log.info("已完成" + i + "个");
              log.info("剩余" + (articles.size() - i) + "个");
              // 随机时间睡眠
              Random rand = new Random();
              int randomNum = rand.nextInt(4000 - 2000 + 1) + 2000; // [2000, 4000]
              log.info("随机等待" + randomNum + "ms");
              Thread.sleep(randomNum);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("生成PDF时出错: " + e.getMessage());
      e.printStackTrace();
    }

    JsonUtils.writeJsonFile(errorarticles);
    log.info("PDF生成完毕！出现{}个失败, 已记录到error文件", errorarticles.size());
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
        SwingUtilities.invokeLater(() -> logTextArea.append(logMessage));
      }
    };
    swingAppender.setContext(rootLogger.getLoggerContext());
    swingAppender.start();

    rootLogger.addAppender(swingAppender);
  }

}