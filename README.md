# urltopdf

一个将 url网页内容 转换为 PDF 文件的 Java 应用程序。

## 项目简介

urltopdf 是一个轻量级的 Java 应用程序，专门用于将 url网页内容 转换为 PDF 文件。该工具可以批量处理文章链接，自动滚动页面以触发懒加载内容，并生成高质量的 PDF 文档。

## 功能特点

- 批量处理url链接
- 自动滚动页面以确保所有内容加载完成
- 生成高质量 A4 格式 PDF 文件
- 图形界面显示实时处理日志
- 支持打包为 Windows EXE 可执行文件，便于在 Windows 系统上直接运行
- 支持打包为 macOS DMG 安装包，便于在 macOS 系统上直接运行

## 技术栈

- Java 17
- Maven
- Playwright Java (网页渲染和PDF生成)

## 安装与构建

### 环境要求

- JDK 17 或更高版本
- Maven 3.x

### 构建步骤

1. 克隆或下载项目源码
2. 进入项目根目录
3. 执行 Maven 构建命令：

```bash
mvn clean package
```

构建完成后，会在 [target](file:///D:/code/urltopdf/urltopdf/target) 目录下生成可执行的 JAR 文件。

## 打包为可执行文件

项目支持打包为 Windows EXE 文件或 macOS DMG 文件。

### 打包为 Windows EXE 文件

要打包为 Windows EXE 文件，需要先注释掉 pom.xml 中 Mac OS 打包插件配置，然后执行：

```bash
mvn clean package
```

EXE 文件会位于 [target](file:///D:/code/urltopdf/urltopdf/target) 目录下，文件名为 `urltopdf.exe`。

### 打包为 macOS DMG 文件

要打包为 macOS DMG 文件，需要先注释掉 pom.xml 中 Windows EXE 打包插件配置，然后执行：

```bash
mvn clean package
```

DMG 文件会位于 [target](file:///D:/code/urltopdf/urltopdf/target) 目录下，文件名为 `urltopdf.dmg`。


## 使用方法

### 准备数据文件

在桌面创建一个名为 `urltopdf` 的文件夹，并在其中放置 [微信公众号文章.json](file:///D:/code/urltopdf/urltopdf/src/main/%E5%BE%AE%E4%BF%A1%E5%85%AC%E4%BC%97%E5%8F%B7%E6%96%87%E7%AB%A0.json) 的 JSON 文件，文件格式如下：

```json
[
  {
    "title": "文章标题",
    "link": "文章链接",
    "create_time": 1754832537
  }
]
```

其中 `create_time` 是 Unix 时间戳格式。

### 运行程序

如果使用 JAR 文件运行：

```bash
java -jar target/urltopdf-1.0-SNAPSHOT.jar
```

如果使用 EXE 文件：

```
target/urltopdf.exe
```
如果使用 dmg 文件：
```
target/urltopdf.dmg
```

程序会读取桌面上的 JSON 文件，依次处理每篇文章链接，并将生成的 PDF 文件保存在桌面上的 `urltopdf` 文件夹中。

## 输出文件

生成的 PDF 文件会按照以下命名规则保存：

```
{日期时间}_{文章标题}.pdf
```

## 注意
1. 该程序第一次启动会自动安装 Playwright 运行时环境。
2. 打包exe只能在windows系统，打包dmg只能在mac系统。
