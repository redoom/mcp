package com.example.mcp.tool;

import com.example.mcp.model.KLineData;
import com.example.mcp.util.CsvMerger;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class VvtrDate {
    private static final String API_KEY = System.getProperty("api-key");
    @Autowired
    private ObjectMapper objectMapper;  // Spring 自动装配

    public VvtrDate() {
    }

    @Tool(
            name = "parseCsvSegment",
            description = "解析单个 CSV 文本段为 KLineData 数组"
    )
    public KLineData[] parseCsvSegment(List<String> lines) {
        return lines.stream()
                .map(KLineData::fromCsv)
                .toArray(KLineData[]::new);
    }


    /**
     * 合并 ROOT 目录下所有 CSV 并一次性解析成 KLineData[]
     *
     * @param rootDir CSV 文件所在根目录，例如 CsvMerger.ROOT
     * @return 解析后的 KLineData 数组
     * @throws IOException 如果读取文件失败
     */
    public KLineData[] parseKLineData(String rootDir) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(rootDir));          // :contentReference[oaicite:7]{index=7}
        int chunkSize = 1000;                                                     // 每段最大行数
        List<KLineData> aggregated = new ArrayList<>();

        for (int i = 0; i < allLines.size(); i += chunkSize) {
            List<String> segment = allLines.subList(i, Math.min(i + chunkSize, allLines.size()));  // :contentReference[oaicite:8]{index=8}
            String argsJson = objectMapper.writeValueAsString(Map.of("lines", segment));
            String resp = parseSegmentCallback.call(argsJson);
            KLineData[] part = objectMapper.readValue(resp, KLineData[].class);
            aggregated.addAll(Arrays.asList(part));
        }

        return aggregated.toArray(new KLineData[0]);

    }


    public static void main(String[] args) throws IOException {
        // 合并多个 CSV 文件为一个字符串
        String csvFiles = CsvMerger.mergeCsvFiles(CsvMerger.ROOT);
        // 解析并一次性返回全部 KLineData
        KLineData[] allData = KLineData.parseCsvLines(csvFiles);
        // 演示：打印结果
        System.out.println(Arrays.toString(allData));
    }
}
