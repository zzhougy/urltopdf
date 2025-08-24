package com.zhou.urltopdf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PDF压缩演示程序，提供图形界面来演示和使用PDF压缩功能
 */
public class PDFCompressorUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextArea logTextArea;
    private JComboBox<String> compressionLevelComboBox;
    private JFileChooser fileChooser;
    private JButton selectFileButton;
    private JButton selectFolderButton;
    private JButton compressButton;
    private File selectedFile;
    private File selectedFolder;

    public PDFCompressorUI() {
        super("PDF压缩演示");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 创建UI组件
        createUIComponents();

        // 添加事件监听器
        addEventListeners();

        // 显示欢迎信息
        logMessage("欢迎使用PDF压缩演示程序\n");
        logMessage("本程序基于Apache PDFBox实现\n");
        logMessage("支持多种压缩策略：图片优化、元数据移除、内容流优化等\n");
        logMessage("请选择要压缩的PDF文件或文件夹，然后点击'开始压缩'按钮\n");
    }

    private void createUIComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // 创建压缩等级选择框
        JLabel levelLabel = new JLabel("压缩等级:");
        String[] compressionLevels = {"低 - 保持高质量", "中 - 平衡质量和大小", "高 - 牺牲部分质量", "极限 - 可能明显影响质量", "超极限 - 最大化减小文件大小"};
        compressionLevelComboBox = new JComboBox<>(compressionLevels);
        compressionLevelComboBox.setSelectedIndex(1); // 默认选择中等压缩

        // 创建文件选择按钮
        selectFileButton = new JButton("选择文件");
        selectFolderButton = new JButton("选择文件夹");
        compressButton = new JButton("开始压缩");
        compressButton.setEnabled(false);

        // 创建文件选择器
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF文件 (*.pdf)", "pdf"));

        // 添加组件到控制面板
        controlPanel.add(levelLabel);
        controlPanel.add(compressionLevelComboBox);
        controlPanel.add(selectFileButton);
        controlPanel.add(selectFolderButton);
        controlPanel.add(compressButton);

        // 创建日志区域
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        // 添加面板到主窗口
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加信息面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        JTextArea infoTextArea = new JTextArea(
                "💡 提示: \n" +
                        "1. 低压缩率适合需要保持高质量的文档\n" +
                        "2. 中等压缩率是大多数文档的最佳选择\n" +
                        "3. 高压缩率适合对文件大小有严格要求的场景\n" +
                        "4. 极限压缩率仅适用于对质量要求不高的场景\n" +
                        "5. 超极限压缩率会最大化减小文件大小，但可能显著影响文档质量\n" +
                        "   （超极限压缩会应用额外策略：所有图片下采样至72DPI、强制转换为灰度、移除所有注释和交互元素）\n"
        );
        infoTextArea.setEditable(false);
        infoTextArea.setOpaque(false);
        infoTextArea.setFont(new Font("Dialog", Font.PLAIN, 11));
        infoPanel.add(infoTextArea, BorderLayout.CENTER);

        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // 设置窗口内容面板
        setContentPane(mainPanel);
    }

    private void addEventListeners() {
        // 选择文件按钮事件
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(PDFCompressorUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    selectedFolder = null;
                    logMessage("已选择文件: " + selectedFile.getAbsolutePath());
                    compressButton.setEnabled(true);
                }
            }
        });

        // 选择文件夹按钮事件
        selectFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(PDFCompressorUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFolder = fileChooser.getSelectedFile();
                    selectedFile = null;
                    logMessage("已选择文件夹: " + selectedFolder.getAbsolutePath());
                    compressButton.setEnabled(true);
                }
            }
        });

        // 开始压缩按钮事件
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 禁用压缩按钮，防止重复点击
                compressButton.setEnabled(false);

                // 在新线程中执行压缩任务，避免UI卡死
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 获取选择的压缩等级
                            int selectedLevelIndex = compressionLevelComboBox.getSelectedIndex();
                            PDFCompressor.CompressionLevel level;

                            switch (selectedLevelIndex) {
                                case 0: level = PDFCompressor.CompressionLevel.LOW; break;
                                case 1: level = PDFCompressor.CompressionLevel.MEDIUM; break;
                                case 2: level = PDFCompressor.CompressionLevel.HIGH; break;
                                case 3: level = PDFCompressor.CompressionLevel.EXTREME; break;
                                case 4: level = PDFCompressor.CompressionLevel.ULTRA_EXTREME; break;
                                default: level = PDFCompressor.CompressionLevel.MEDIUM; break;
                            }

                            logMessage("\n====== 开始压缩任务 ======");
                            logMessage("选择的压缩等级: " + level);

                            // 执行压缩任务
                            if (selectedFile != null) {
                                // 压缩单个文件
                                compressSingleFile(selectedFile, level);
                            } else if (selectedFolder != null) {
                                // 批量压缩文件夹
                                batchCompressFolder(selectedFolder, level);
                            }

                            logMessage("====== 压缩任务完成 ======\n");
                        } catch (Exception ex) {
                            logMessage("压缩过程中发生错误: " + ex.getMessage());
                            ex.printStackTrace();
                        } finally {
                            // 恢复压缩按钮状态
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    compressButton.setEnabled(true);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    private void compressSingleFile(File inputFile, PDFCompressor.CompressionLevel level) {
        try {
            logMessage("开始压缩文件: " + inputFile.getName());

            // 创建输出文件，添加时间戳避免覆盖
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputPath = inputFile.getAbsolutePath().replace(".pdf", "_compressed_" + timestamp + ".pdf");
            File outputFile = new File(outputPath);

            // 执行压缩
            PDFCompressor.compressPdf(inputFile, outputFile, level);

            logMessage("文件压缩完成，保存路径: " + outputPath);

            // 显示压缩结果对话框
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(
                            PDFCompressorUI.this,
                            "PDF文件压缩完成！\n" +
                                    "原始文件: " + inputFile.getName() + "\n" +
                                    "压缩后文件: " + outputFile.getName() + "\n" +
                                    "压缩率: " + String.format("%.2f%%",
                                    (1 - (double) outputFile.length() / inputFile.length()) * 100),
                            "压缩完成",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });
        } catch (IOException ex) {
            logMessage("压缩文件时出错: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void batchCompressFolder(File folder, PDFCompressor.CompressionLevel level) {
        try {
            logMessage("开始批量压缩文件夹中的PDF文件: " + folder.getAbsolutePath());

            // 调用PDFCompressor的批量压缩方法
            PDFCompressor.batchCompressPdfs(folder.getAbsolutePath(), level);

            // 显示批量压缩结果对话框
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(
                            PDFCompressorUI.this,
                            "文件夹中的PDF文件批量压缩完成！\n" +
                                    "文件夹路径: " + folder.getAbsolutePath() + "\n" +
                                    "压缩等级: " + level,
                            "批量压缩完成",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });
        } catch (Exception ex) {
            logMessage("批量压缩时出错: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void logMessage(final String message) {
        // 在事件调度线程中更新UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logTextArea.append(message + "\n");
                // 自动滚动到最新内容
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            }
        });
    }

    /**
     * 主方法，启动PDF压缩演示程序
     */
    public static void main(String[] args) {
        // 设置外观为系统默认
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在事件调度线程中创建和显示UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PDFCompressorUI demo = new PDFCompressorUI();
                demo.setVisible(true);
            }
        });
    }
}