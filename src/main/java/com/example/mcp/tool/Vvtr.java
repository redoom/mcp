package com.example.mcp.tool;

import com.example.mcp.model.DataBack;
import com.example.mcp.model.DataLabel;
import com.example.mcp.repository.VvtrData;
import com.example.mcp.util.CsvMerger;
import com.example.mcp.util.FolderSize;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Vvtr {
    private static final String API_KEY = System.getProperty("api-key");

    private final VvtrData vvtrData;

    // 使用构造器注入依赖
    public Vvtr(VvtrData vvtrData) {
        this.vvtrData = vvtrData;
    }

    @Tool(name = "get-financial-products-data-path",
            description = "获取所需金融产品历史数据的资源路径")
    public List<String> getFundData(@ToolParam(required = true, description = "查询的金融产品种类,eg:fund") String type,
                                  @ToolParam(required = true, description = "查询的数据类型,eg:15m,1m,1d,tick") String name,
                                  @ToolParam(required = true, description = "种类代码,可以为空字符串") String symbol,
                                  @ToolParam(required = true, description = "查询的开始时间(yyyyMMdd),可为空字符串") String startTime,
                                  @ToolParam(required = true, description = "查询的结束时间(yyyyMMdd),可为空字符串") String endTime) throws Exception {
        // 获取到所有需要查找的文件
        Path rootDir = Paths.get(CsvMerger.ROOT, type, name);


        if (startTime == null || startTime.isEmpty()) {
            startTime = "00000000";
        }
        if (endTime == null || endTime.isEmpty()) {
            endTime = "99999999";
        }
        if (name.equals("1d")) {
            List<Path> paths = CsvMerger.findAllCsvFiles(rootDir, startTime, endTime);
            return paths.stream()
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } else {
            List<Path> paths = CsvMerger.findAllCsvFiles(rootDir, startTime, endTime, symbol);
            return paths.stream()
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }


    @Tool(name = "get-financial-products-data-count",
            description = "根据获取的金融产品资源路径查询数据条数(除了1d,其他的均为估计值)")
    public long getDataCount(@ToolParam(description = "要查询的资源路径,eg:[D:/data/fund/1m/202009/20200904/20200904.csv]") List<String> pathStrs,
                             @ToolParam(description = "要查询的数据类型,eg:1d,1m,15m,tick") String type,
                             @ToolParam(required = false, description = "仅仅1d需要,种类代码") String symbol) throws Exception {

        List<Path> paths = pathStrs.stream()
                .map(Paths::get)
                .collect(Collectors.toList());
        Long size = FolderSize.getFileSize(paths);
        switch (type) {
            case "1d":
                String data = CsvMerger.parseMultipleCSVFilesWithoutHeader(paths);
                String filterData = CsvMerger.filterData(data, symbol);
                return CsvMerger.countLines(filterData);
            case "1m", "15m":
                return size / 120;
            case "tick":
                return size / 232;
            default:
                return 0L;
        }
    }

    @Tool(name = "get-financial-products-min-data",
            description = "根据获取的分钟(1m/15m)类型金融产品资源路径查询数据,分片查询,受到上下文限制,一般需要多次请求,一次性查询不超过3000条,超过3000条分多次查询,会返回剩下需要查询的文件,返回文件为空即查完")
    public DataBack getMinuteData(@ToolParam(description = "要查询的资源路径,eg:[D:/data/fund/1m/202009/20200904/20200904.csv]") List<String> pathStrs,
                                  @ToolParam(description = "查询的开始时间(yyyy-MM-dd HH:mm:ss),如果为空字符串则查询全部数据") String startTime,
                                  @ToolParam(description = "查询的结束时间(yyyy-MM-dd HH:mm:ss),如果为空字符串则查询全部数据") String endTime) throws Exception {
        // 先把字符串列表转回 Path
        List<Path> paths = pathStrs.stream()
                .map(Paths::get)
                .collect(Collectors.toList());
        if (startTime == null || startTime.isEmpty()) {
            startTime = null;
        }
        if (endTime == null || endTime.isEmpty()) {
            endTime = null;
        }
        int bobIndex = CsvMerger.getBobIndex(paths.get(0));
        return vvtrData.getMinData(paths, startTime, endTime, bobIndex);
    }

    @Tool(name = "get-financial-products-day-data",
            description = "根据获取的日线(1d)类型金融产品资源路径查询数据,分片查询，一次性查询不超过3000条,超过3000条分多次查询,会返回剩下需要查询的文件,返回文件为空即查完")
    public DataBack getDayData(@ToolParam(description = "要查询的资源路径,eg:[D:/data/fund/1d/202009/20200904/20200904.csv]") List<String> pathStrs,
                             @ToolParam(description = "种类代码") String symbol,
                             @ToolParam(required = false, description = "查询的开始时间(yyyy-MM-dd),如果为空字符串则查询全部数据") String startTime,
                             @ToolParam(required = false, description = "查询的结束时间(yyyy-MM-dd),如果为空字符串则查询全部数据") String endTime) throws Exception {
        // 先把字符串列表转回 Path
        List<Path> paths = pathStrs.stream()
                .map(Paths::get)
                .collect(Collectors.toList());
        int bobIndex = CsvMerger.getBobIndex(paths.get(0));
        int symbolIndex = CsvMerger.getSymbolIndex(paths.get(0));
//        String data = CsvMerger.parseMultipleCSVFilesWithoutHeader(paths);
//        String filterData = CsvMerger.filterData(data, symbol);
        return vvtrData.getDayData(paths, symbol, symbolIndex, startTime, endTime, bobIndex);
    }

    @Tool(name = "get-financial-products-tick-data",
    description = "根据获取的每一笔成交数据(tick)类型金融产品资源路径查询数据,分片查询，一次性查询不超过180条,超过180条分多次查询,会返回当前文件及剩下需要查询的文件,和当前文件的索引,返回文件为空即查完")
    public DataLabel getTickData(@ToolParam(description = "要查询的资源路径,eg:[D:/data/fund/tick/202009/20200904/20200904.csv]") List<String> pathStrs,
                                @ToolParam(description = "查询的开始时间(yyyy-MM-dd HH:mm:ss),如果为空字符串则查询全部数据") String startTime,
                                @ToolParam(description = "查询的结束时间(yyyy-MM-dd HH:mm:ss),如果为空字符串则查询全部数据") String endTime,
                                @ToolParam(description = "上一次剩余数据,第一次则为0") int nextIndex,
                                @ToolParam(required = false, description = "要获取的条数") int count) throws Exception {
        List<Path> paths = pathStrs.stream()
                .map(Paths::get)
                .collect(Collectors.toList());
        int createTimeIndex = CsvMerger.getCreateTimeIndex(paths.get(0));
        return vvtrData.getTickData(paths, startTime, endTime, createTimeIndex, nextIndex, count);
    }

    public static void main(String[] args) throws Exception {

    }

}
