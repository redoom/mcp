package com.example.mcp.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvMerger {

    // 指定要扫描的目录
    public static final String ROOT = "D:\\data\\crypto";

    /**
     * 程序入口：遍历 ROOT 下所有 .csv 文件，
     * 跳过每个文件的第一行表头，
     * 将所有记录拼接成一个大字符串，并以 '\n' 分隔，直接打印全部。
     */
    public static void main(String[] args) {
        try {
            List<Path> megaString = findAllCsvFiles(Path.of(ROOT));
            String csvFiles = mergeCsvFiles(Path.of(ROOT));
            // 获取桌面路径
            String desktopPath = System.getProperty("user.home") + "/Desktop";
            File outputFile = new File(desktopPath, "csv_files.txt");


            // 直接把所有数据一次性打印出来
            System.out.println(megaString);
        } catch (IOException e) {
            System.err.println("❌ 合并 CSV 失败： " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 遍历目录下所有 .csv 文件，合并其数据为一个字符串
     * 自动跳过每个文件的第一行
     */
    public static String mergeCsvFiles(Path rootDir) throws IOException {
        try (Stream<Path> files = Files.walk(rootDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase().endsWith(".csv"))) {

            return files
                    // 对每个文件，解析为 Stream<String>，再 flatMap 汇总
                    .flatMap(CsvMerger::parseFileAsStream)
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * 把单个 CSV 文件解析为 Stream<String>，
     * 每条记录转成 “字段1,字段2,...” 的字符串，
     * 并自动跳过第一行表头
     */
    private static Stream<String> parseFileAsStream(Path csvPath) {
        try {
            CSVParser parser = CSVParser.parse(
                    csvPath,
                    StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT
                            .withFirstRecordAsHeader()   // ← 跳过表头
            );
            return parser.stream()
                    .map(record -> String.join(",", record));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Path> findAllCsvFiles(Path rootDir) throws IOException {
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 根据指定的日期范围在目录中查找所有CSV文件。
     *
     * @param rootDir 要搜索的根目录
     * @param startDate 过滤的开始日期（包含），格式为 "yyyyMMdd"
     * @param endDate 过滤的结束日期（包含），格式为 "yyyyMMdd"
     * @return 表示过滤后的CSV文件的Path对象列表
     * @throws IOException 如果发生I/O错误
     */
    public static List<Path> findAllCsvFiles(Path rootDir, String startDate, String endDate) throws IOException {
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .filter(path -> {
                        try {
                            // 获取文件名（不含扩展名）
                            String fileName = path.getFileName().toString();
                            String file = fileName.substring(0, fileName.lastIndexOf('.'));

                            // 获取文件所在的目录
                            Path parent = path.getParent();

                            // 如果父目录为空，无法过滤
                            if (parent == null) return false;

                            // 获取父目录的名称（这应该是日期格式，例如 20200101）
                            String dateStr = parent.getFileName().toString();

                            // 确保日期格式正确
                            if (dateStr.length() != 8) return false;

                            // 使用字符串比较判断日期是否在范围内
                            return dateStr.compareTo(startDate) >= 0 &&
                                    dateStr.compareTo(endDate) <= 0;
                        } catch (Exception e) {
                            return false; // 如果日期提取或解析失败则跳过
                        }
                    })
                    .collect(Collectors.toList());
        }
    }
    /**
     * 根据指定的日期范围在目录中查找所有CSV文件。
     *
     * @param rootDir 要搜索的根目录
     * @param startDate 过滤的开始日期（包含），格式为 "yyyyMMdd"
     * @param endDate 过滤的结束日期（包含），格式为 "yyyyMMdd"
     * @param symbol 过滤产品代码
     * @return 表示过滤后的CSV文件的Path对象列表
     * @throws IOException 如果发生I/O错误
     */
    public static List<Path> findAllCsvFiles(Path rootDir, String startDate, String endDate, String symbol) throws IOException {
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .filter(path -> {
                        try {
                            // 获取文件名（不含扩展名）
                            String fileName = path.getFileName().toString();
                            String file = fileName.substring(0, fileName.lastIndexOf('.'));

                            if (!file.equals(symbol)) return false;

                            // 获取文件所在的目录
                            Path parent = path.getParent();

                            // 如果父目录为空，无法过滤
                            if (parent == null) return false;

                            // 获取父目录的名称（这应该是日期格式，例如 20200101）
                            String dateStr = parent.getFileName().toString();

                            // 确保日期格式正确
                            if (dateStr.length() != 8) return false;

                            // 使用字符串比较判断日期是否在范围内
                            return dateStr.compareTo(startDate) >= 0 &&
                                    dateStr.compareTo(endDate) <= 0;
                        } catch (Exception e) {
                            return false; // 如果日期提取或解析失败则跳过
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * 解析CSV文件（跳过表头）并将所有数据行连接成一个字符串
     * @param path CSV文件路径
     * @return 包含所有数据行（不含表头）的字符串，行与行之间用\n分隔
     */
    public static String parseCSVWithoutHeaderAsString(Path path) {
        StringBuilder result = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;
            boolean firstDataLine = true;

            while ((line = br.readLine()) != null) {
                // 跳过第一行（表头）
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // 对于数据行，添加到结果字符串中
                if (firstDataLine) {
                    result.append(line);
                    firstDataLine = false;
                } else {
                    result.append("\n").append(line);
                }
            }
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * 如果需要处理多个CSV文件
     * @param paths CSV文件路径列表
     * @return 包含所有文件数据的字符串（不含表头）
     */
    public static String parseMultipleCSVFilesWithoutHeader(List<Path> paths) {
        StringBuilder allData = new StringBuilder();
        boolean firstFile = true;

        for (Path path : paths) {
            String fileData = parseCSVWithoutHeaderAsString(path);

            if (!fileData.isEmpty()) {
                if (firstFile) {
                    allData.append(fileData);
                    firstFile = false;
                } else {
                    // 在不同文件之间添加换行符
                    allData.append("\n").append(fileData);
                }
            }
        }

        return allData.toString();
    }

    /**
     * 统计一共有多少行数据
     * @param text 需要统计的文本
     * @return 返回行数
     */
    public static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 按换行符分割并计算结果数组长度
        return text.split("\n", -1).length;
    }
}
