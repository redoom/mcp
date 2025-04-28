package com.example.mcp.model;

import com.example.mcp.util.CsvMerger;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@JsonIgnoreProperties(ignoreUnknown = true)
public record KLineData(
        String symbol,
        double open,
        double high,
        double low,
        double close,
        double amount,
        double volume,
        LocalDateTime bob,
        LocalDateTime eob,
        int type
) {
    private static final DateTimeFormatter[] CSV_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

    public static LocalDateTime parseDate(String text) {
        for (DateTimeFormatter fmt : CSV_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("无法解析日期: " + text);
    }

    /**
     * 从一行 CSV／制表符分隔、逗号分隔文本解析生成 KLineData。
     */
    public static KLineData fromCsv(String line) {
        // 同时支持逗号和 Tab 分隔
        String[] parts = line.split("[,\t]", -1);
        if (parts.length < 10) {
            throw new IllegalArgumentException("CSV 字段不足（至少 10 列），出错行：" + line);
        }
        return new KLineData(
                parts[0],
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]),
                Double.parseDouble(parts[6]),
                parseDate(parts[7]),
                parseDate(parts[8]),
                Integer.parseInt(parts[9])
        );
    }

    /**
     * 使用 Jackson 的 JsonNode 解析生成 KLineData。
     */
    public static KLineData fromJson(JsonNode node) {
        return new KLineData(
                node.get("symbol").asText(),
                node.get("open").asDouble(),
                node.get("high").asDouble(),
                node.get("low").asDouble(),
                node.get("close").asDouble(),
                node.get("amount").asDouble(),
                node.get("volume").asDouble(),
                parseDate(node.get("bob").asText()),
                parseDate(node.get("eob").asText()),
                node.get("type").asInt()
        );
    }

    /**
     * 解析多行文本（支持 \r?\n 分隔），过滤空行后生成 KLineData 数组。
     */
    public static KLineData[] parseCsvLines(String lines) {
        return Arrays.stream(lines.split("\\r?\\n"))
                .filter(line -> !line.isBlank())
                .map(KLineData::fromCsv)
                .toArray(KLineData[]::new);
    }
    public static void main(String[] args) throws IOException {
        String csvFiles = CsvMerger.mergeCsvFiles(Path.of(CsvMerger.ROOT));
        List<KLineData> allData = List.of(KLineData.parseCsvLines(csvFiles));
        System.out.println(allData);
    }

}
