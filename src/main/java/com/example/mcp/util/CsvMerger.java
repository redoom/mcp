package com.example.mcp.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvMerger {

    // 指定要扫描的目录
    public static final Path ROOT = Paths.get("D:\\data\\crypto");

    /**
     * 程序入口：遍历 ROOT 下所有 .csv 文件，
     * 跳过每个文件的第一行表头，
     * 将所有记录拼接成一个大字符串，并以 '\n' 分隔，直接打印全部。
     */
    public static void main(String[] args) {
        try {
            String megaString = mergeCsvFiles(ROOT);
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
}
