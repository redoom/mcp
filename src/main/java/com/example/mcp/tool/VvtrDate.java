package com.example.mcp.tool;

import com.example.mcp.model.KLineData;
import com.example.mcp.util.CsvMerger;
import com.example.mcp.util.FolderSize;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.Csv;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class VvtrDate {
//    private static final String API_KEY = System.getProperty("api-key");

    public VvtrDate() {
    }
    @Tool(name = "get-fund-data",
    description = "获取所需金融产品历史数据的资源路径")
    public List<Path> getFundData(@ToolParam(required = true, description = "查询的金融产品种类") String type,
                           @ToolParam(required = true, description = "查询的数据类型,例如:15m,1m") String name,
                           @ToolParam(required = false, description = "种类代码") String symbol,
                           @ToolParam(required = false, description = "查询的开始时间(yyyyMMdd)") String startTime,
                           @ToolParam(required = false, description = "查询的结束时间(yyyyMMdd)") String endTime) throws Exception {
        // 获取到所有需要查找的文件
        String uri = CsvMerger.ROOT + type + "//" + name;
        if (startTime == null && startTime.equals("")) {
            startTime = "00000000";
        }
        if (endTime == null && endTime.equals("")) {
            endTime = "99999999";
        }
        List<Path> allCsvFiles = CsvMerger.findAllCsvFiles(Path.of(uri), startTime, endTime, symbol);
        return allCsvFiles;
    }

    @Tool(name = "get-fund-data",
    description = "获取金融产品1d的历史数据的资源路径")
    public int getData(@ToolParam(required = true, description = "查询的金融产品种类") String type,
                           @ToolParam(required = false, description = "种类代码") String symbol,
                           @ToolParam(required = false, description = "查询的开始时间(yyyyMMdd)") String startTime,
                           @ToolParam(required = false, description = "查询的结束时间(yyyyMMdd)") String endTime) throws Exception {
        return 0;
    }

    @Tool(name = "Get-K-Line-Count",
    description = "返回想要获取k线的数量用于分片" +
            "传入参数：" +
            "symbol: 交易对，例如 AIOZUSD (如果为'-1'则是所有数据)" +
            "startTime: 开始时间，例如 2023-05-01 00:00:00 (如果为'-1'则是从已知的最早数据开始)" +
            "endTime: 结束时间，例如 2023-05-10 00:00:00 (如果为'-1'则是直到最新的数据)"
            )
    public int getKLineCount(String symbol, String startTime, String endTime) throws IOException {
        // 获取所有数据并过滤
        String csvFiles = CsvMerger.mergeCsvFiles(Path.of(CsvMerger.ROOT));
        KLineData[] allData = KLineData.parseCsvLines(csvFiles);
        if (symbol != null && !symbol.isEmpty() && !symbol.equals("-1")) {
            symbol = "X:" + symbol;
        }
        // 解析起始时间
        LocalDateTime startDateTime = null;
        if (startTime != null && !startTime.isEmpty()) {
            if (startTime.equals("-1")) {
                // 找出数据中的最早时间
                startDateTime = Arrays.stream(allData)
                        .map(KLineData::bob)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);
            } else {
                try {
                    startDateTime = KLineData.parseDate(startTime);
                } catch (Exception e) {
                    System.err.println("起始时间格式错误: " + e.getMessage());
                    throw new IllegalArgumentException("起始时间格式错误", e);
                }
            }
        }

        // 解析终止时间
        LocalDateTime endDateTime = null;
        if (endTime != null && !endTime.isEmpty()) {
            if (endTime.equals("-1")) {
                // 如果终止时间为-1，找出数据中的最新时间
                endDateTime = Arrays.stream(allData)
                        .map(KLineData::bob)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
            } else {
                try {
                    endDateTime = KLineData.parseDate(endTime);
                } catch (Exception e) {
                    System.err.println("终止时间格式错误: " + e.getMessage());
                    throw new IllegalArgumentException("终止时间格式错误", e);
                }
            }
        }
        // 计算符合条件的数据数量
        int count = 0;
        for (KLineData data : allData) {
            // 符号匹配
            boolean symbolMatch = symbol == null || symbol.isEmpty() || symbol.equals("-1") ||
                    data.symbol().equals(symbol);

            // 时间匹配
            boolean timeMatch = true;
            LocalDateTime dataTime = data.bob();

            if (startDateTime != null && dataTime.isBefore(startDateTime)) {
                timeMatch = false;
            }

            if (endDateTime != null && dataTime.isAfter(endDateTime)) {
                timeMatch = false;
            }

            if (symbolMatch && timeMatch) {
                count++;
            }
        }

        return count;
    }

    @Tool(name = "Get-K-Line-Data",
            description = "返回K线数据" +
                    "传入参数：" +
                    "symbol: 交易对，例如 AIOZUSD (如果为'-1'则是所有数据)" +
                    "startTime: 开始时间，例如 2023-05-01 00:00:00 (如果为'-1'则是从已知的最早数据开始)" +
                    "endTime: 结束时间，例如 2023-05-10 00:00:00 (如果为'-1'则是直到最新的数据)" +
                    "offset: 起始位置" +
                    "limit: 获取数量(最大3000)" +
                    "分页用于避免一次性返回大量数据导致报错"
                    )
    public List<KLineData> getKLineData(String symbol, String startTime, String endTime,
                                               int offset, int limit) throws IOException {

        int actualLimit = Math.min(limit, 3000);

        // 获取所有数据
        String csvFiles = CsvMerger.mergeCsvFiles(Path.of(CsvMerger.ROOT));
        KLineData[] allData = KLineData.parseCsvLines(csvFiles);
        List<KLineData> data = new ArrayList<>();
        if (symbol != null && !symbol.isEmpty() && !symbol.equals("-1")) {
            symbol = "X:" + symbol;
        }
        LocalDateTime startDateTime = null;
        if (startTime != null && !startTime.isEmpty()) {
            if (startTime.equals("-1")) {
                // 找出数据中的最早时间
                startDateTime = Arrays.stream(allData)
                        .map(KLineData::bob)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);
            } else {
                try {
                    startDateTime = KLineData.parseDate(startTime);
                } catch (Exception e) {
                    System.err.println("起始时间格式错误: " + e.getMessage());
                    throw new IllegalArgumentException("起始时间格式错误", e);
                }
            }
        }
        LocalDateTime endDateTime = null;
        if (endTime != null && !endTime.isEmpty()) {
            if (endTime.equals("-1")) {
                // 如果终止时间为-1，找出数据中的最新时间
                endDateTime = Arrays.stream(allData)
                        .map(KLineData::bob)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
            } else {
                try {
                    endDateTime = KLineData.parseDate(endTime);
                } catch (Exception e) {
                    System.err.println("终止时间格式错误: " + e.getMessage());
                    throw new IllegalArgumentException("终止时间格式错误", e);
                }
            }
        }

        // 应用过滤器
        for (KLineData item : allData) {
            // 符号匹配
            boolean symbolMatch = symbol == null || symbol.isEmpty() || symbol.equals("-1") ||
                    item.symbol().equals(symbol);

            // 时间匹配
            boolean timeMatch = true;
            LocalDateTime dataTime = item.bob();

            if (startDateTime != null && dataTime.isBefore(startDateTime)) {
                timeMatch = false;
            }

            if (endDateTime != null && dataTime.isAfter(endDateTime)) {
                timeMatch = false;
            }

            if (symbolMatch && timeMatch) {
                data.add(item);
            }
        }
        // 应用分页
        int startIndex = offset;
        int endIndex = Math.min(startIndex + actualLimit, data.size());

        // 检查是否超出边界
        if (startIndex >= data.size()) {
            return new ArrayList<>(); // 返回空数组
        }
        return List.of(data.subList(startIndex, endIndex).toArray(new KLineData[0]));

    }

    @Tool(name = "test-string-get",
    description = "直接通过字符串的方式返回所有")
    public String getStringData() throws IOException {
        return CsvMerger.mergeCsvFiles(Path.of(CsvMerger.ROOT));
    }

    @Tool(name = "get-csv-file",
    description = "获取加密货币历史价格数据集csv文件")
    public List<Path> getFiles() throws IOException {
        return CsvMerger.findAllCsvFiles(Path.of(CsvMerger.ROOT));
    }

    public static void main(String[] args) throws IOException {
        // 测试参数
        String symbol = null; // 所有交易对
        String startTime = "2023-01-01 00:00:00";
        String endTime = "2024-12-31 23:59:59";

        // 1. 测试获取K线数量
        VvtrDate tools = new VvtrDate();
        String data = tools.getStringData();
        int halfLength = data.length() / 2;
        String firstHalf = data.substring(0, halfLength);
        System.out.println("符合条件的K线总数量: " + firstHalf);
        // 尝试获取特定交易对的数据
        String specificSymbol = "AIOZUSD";
        List<KLineData> btcData = tools.getKLineData("-1", "-1", "-1", 0, 5000);
        System.out.println("\n" + specificSymbol + " 交易对数据 (前10条): " + btcData);
    }
}
