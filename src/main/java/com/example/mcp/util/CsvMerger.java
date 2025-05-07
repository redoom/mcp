package com.example.mcp.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvMerger {

    // 指定要扫描的目录
    public static final String ROOT = "D:\\data\\";
//    public static final String ROOT = System.getProperty("user.data.path");

    /**
     * 程序入口：遍历 ROOT 下所有 .csv 文件，
     * 跳过每个文件的第一行表头，
     * 将所有记录拼接成一个大字符串，并以 '\n' 分隔，直接打印全部。
     */
    public static void main(String[] args) {
        try {
            Path rootDir = Path.of(ROOT);
            List<Path> megaString = findAllCsvFiles(rootDir);
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
        boolean returnAll = startDate.equals("00000000") && endDate.equals("99999999");
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .filter(path -> {

                        try {
                            // 获取文件所在的目录
                            Path parent = path.getParent();
                            // 如果父目录为空，无法过滤
                            if (parent == null) return false;

                            // 获取父目录的名称（这应该是日期格式，例如 20200101）
                            String dateStr = parent.getFileName().toString();

                            if (rootDir.getFileName().toString().equals(dateStr)) return true;

                            // 确保日期格式正确
                            if (dateStr.length() != 8) return false;

                            if (returnAll) return true;

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
        boolean returnAll = startDate.equals("00000000") && endDate.equals("99999999");
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .filter(path -> {



                        try {
                            // 获取文件名（不含扩展名）
                            String fileName = path.getFileName().toString();
                            String file = fileName.substring(0, fileName.lastIndexOf('.'));

                            if (symbol.isEmpty()) return true;

                            if (!file.equals(symbol)) return false;

                            // 获取文件所在的目录
                            Path parent = path.getParent();

                            // 如果父目录为空，无法过滤
                            if (parent == null) return false;

                            // 获取父目录的名称（这应该是日期格式，例如 20200101）
                            String dateStr = parent.getFileName().toString();

                            if (rootDir.getFileName().toString().equals(dateStr)) return true;

                            // 确保日期格式正确
                            if (dateStr.length() != 8) return false;

                            if (returnAll) return true;

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

            // 在循环结束后添加
            if (!firstDataLine) {
                result.append("\n");
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

        // 检查文本是否以换行符结尾
        if (text.endsWith("\n")) {
            // 如果以换行符结尾，减去多余的一行
            return text.split("\n", -1).length - 1;
        } else {
            // 如果不以换行符结尾，直接返回分割结果
            return text.split("\n", -1).length;
        }
    }

    /**
     * 1d使用，读取到所有的数据获取指定
     * @param data 数据
     * @param symbol 种类代码用于过滤
     * @return 过滤后的数据
     */
/*
    public static String filterData(String data, String symbol) {
        if (data == null || data.isEmpty() || symbol == null || symbol.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] lines = data.split("\n");

        for (String line : lines) {
            String[] fields = line.split(",");

            // 确保行有足够的字段
            if (fields.length >= 2) {
                // 检查第二个字段(索引1)是否与symbol匹配
                if (fields[1].equals(symbol)) {
                    // 将匹配的行添加到结果中，并添加换行符
                    result.append(line).append("\n");
                }
            }
        }

        // 如果有匹配行，移除最后一个换行符
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }
*/

    /**
     * 1d使用，读取到所有的数据获取指定
     * @param data 数据
     * @param symbol 种类代码用于过滤
     * @return 过滤后的数据
     */
    public static String filterData(String data, String symbol) {
        if (data == null || data.isEmpty() || symbol == null || symbol.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] lines = data.split("\n");

        // 确保至少有一行（表头）
        if (lines.length == 0) {
            return "";
        }

        // 添加表头到结果
        String header = lines[0];
        result.append(header).append("\n");

        // 解析表头，找到symbol所在的列索引
        String[] headerFields = header.split(",");
        int symbolIndex = -1;

        for (int i = 0; i < headerFields.length; i++) {
            if (headerFields[i].trim().equalsIgnoreCase("symbol")) {
                symbolIndex = i;
                break;
            }
        }

        // 如果没有找到symbol列，返回只有表头的结果
        if (symbolIndex == -1) {
            return result.toString();
        }

        // 遍历数据行（跳过表头）
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] fields = line.split(",");

            // 确保行有足够的字段
            if (fields.length > symbolIndex) {
                // 检查symbol列是否与给定的symbol匹配
                if (fields[symbolIndex].equals(symbol)) {
                    // 将匹配的行添加到结果中，并添加换行符
                    result.append(line).append("\n");
                }
            }
        }

        // 如果只有表头，移除最后一个换行符
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    public static int getBobIndex(Path path) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            while ((line = br.readLine()) != null) {
                String[] str = line.split(",");
                for (int i = 0; i < str.length; i++) {
                    if (str[i].trim().equalsIgnoreCase("bob")) {
                        return i;
                    }
                }
                return -1;
            }
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
        }
        return -1;
    }

    public static int getSymbolIndex(Path path) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            while ((line = br.readLine()) != null) {
                String[] str = line.split(",");
                for (int i = 0; i < str.length; i++) {
                    if (str[i].trim().equalsIgnoreCase("symbol")) {
                        return i;
                    }
                }
                return -1;
            }
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
        }
        return -1;
    }

    public static int getCreateTimeIndex(Path path) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            while ((line = br.readLine()) != null) {
                String[] str = line.split(",");
                for (int i = 0; i < str.length; i++) {
                    if (str[i].trim().equalsIgnoreCase("created_at")) {
                        return i;
                    }
                }
                return -1;
            }
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
        }
        return -1;
    }
}
