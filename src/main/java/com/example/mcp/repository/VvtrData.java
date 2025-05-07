package com.example.mcp.repository;

import com.example.mcp.model.DataBack;
import com.example.mcp.model.DataLabel;
import com.example.mcp.util.CsvMerger;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Repository
public class VvtrData {

    /**
     * 获取数据
     *
     * @param paths     解析的文件路径
     * @param nextIndex 上一次的读取位置
     * @param count     一共需要的读取条数
     * @return 数据，下一次的读取位置，剩余文件路径
     */
    public DataLabel getMinuteData(List<Path> paths, int nextIndex, int count) {
        StringBuilder allContent = new StringBuilder(); // 用于存储查询到的所有结果
        StringBuilder resultData = new StringBuilder(); // 用于存储返回的结果
        int totalLinesProcessed = 0; // 总的行数
        int processedPathIndex = 0; // 文件索引
        int line = 0;
        int index = 0;
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            String data = CsvMerger.parseCSVWithoutHeaderAsString(path);
            allContent.append(data);
            int lines = CsvMerger.countLines(data);
            line = lines;
            totalLinesProcessed += lines;
            processedPathIndex = i;
            if (i == 0 && (totalLinesProcessed - nextIndex) > count) {
                break;
            } else if (totalLinesProcessed > count) {
                break;
            }
        }
        // 将合并后的内容按行分割
        String[] allLines = allContent.toString().split("\n");
        int actualLimit = Math.min(count, allLines.length);
        for (int i = nextIndex; i < actualLimit + nextIndex; i++) {
            resultData.append(allLines[i]);
            if (i < actualLimit + nextIndex - 1) {
                resultData.append("\n");
            }
        }
        //
        if (count < allLines.length) {
            index = nextIndex
                    + actualLimit
                    + line
                    - totalLinesProcessed;
            if (index < 0) {
                int lines = CsvMerger.countLines(CsvMerger.parseCSVWithoutHeaderAsString(paths.get(0)));
                index = lines + index;
            }
        }

        List<Path> remainingPaths = new ArrayList<>();
        for (int i = processedPathIndex; i < paths.size(); i++) {
            remainingPaths.add(paths.get(i));
        }
        return new DataLabel(resultData.toString(), index, remainingPaths);
    }

    /**
     * 获取日线数据
     *
     * @param filterData 过滤后的数据，每条数据以\n结尾
     * @param offset     起始位置（第几条开始）
     * @param limit      结束位置（第几条结束）
     * @return 指定范围内的数据
     */
    public String getDayData(String filterData, int offset, int limit) {
        // 将数据按行分割
        String[] lines = filterData.split("\n");
        StringBuilder result = new StringBuilder();

        // 设置默认值
        int startIndex = Math.max(offset, 0);
        int endIndex = (limit > 0) ? Math.min(startIndex + limit, lines.length) : lines.length;

        // 检查数据量是否超过3000条
        if (endIndex - startIndex > 3000) {
            endIndex = startIndex + 3000;
        }

        // 提取指定范围的数据
        for (int i = startIndex; i < endIndex; i++) {
            if (i < lines.length) {
                result.append(lines[i]).append("\n");
            }
        }

        return result.toString();
    }

    public DataBack getDayData(List<Path> paths, String symbol, int symbolIndex, String startTime, String endTime, int bobIndex) {
        int processedPathIndex = 0; // 文件索引
        StringBuilder stringBuilder = new StringBuilder(); // 返回结果
        int totalLine = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 开始时间
        LocalDate startDateTime = null;
        // 结束时间
        LocalDate endDateTime = null;

        try {
            startDateTime = LocalDate.parse(startTime, formatter);
            endDateTime = LocalDate.parse(endTime, formatter);
        } catch (Exception e) {
            System.err.println("日期时间解析失败，将使用字符串比较: " + e.getMessage());
        }
        for (int i = 0; i < paths.size(); i++) {
            processedPathIndex = i;
            Path path = paths.get(i);
            String data = CsvMerger.parseCSVWithoutHeaderAsString(path);
            String[] lines = data.split("\n");
            for (String line : lines) {
                String[] single = line.split(",");
                if (single.length <= bobIndex) {
                    continue;
                }
                // 时间校验
                String startDataTime = single[bobIndex].split(" ")[0];
                String endDataTime = single[bobIndex + 1].split(" ")[0];
                LocalDate startDateLocalTime = LocalDate.parse(startDataTime, formatter);
                LocalDate endDateLocalTime = LocalDate.parse(endDataTime, formatter);
                if (startDateTime != null && endDateTime != null && symbol != null) {
                    if (!startDateLocalTime.isAfter(endDateTime) &&
                            !endDateLocalTime.isBefore(startDateTime) &&
                            // 区别
                            single[symbolIndex].equals(symbol)) {
                        stringBuilder.append(line).append("\n");
                    }
                } else if (symbol != null) {
                    if (single[symbolIndex].equals(symbol)) {
                        stringBuilder.append(line).append("\n");
                    }
                } else {
                    stringBuilder.append(line).append("\n");
                }
            }
        }
        List<Path> remainingPaths = new ArrayList<>();
        for (int i = processedPathIndex + 1; i < paths.size(); i++) {
            remainingPaths.add(paths.get(i));
        }
        return new DataBack(stringBuilder.toString(), remainingPaths);
    }


    public DataBack getMinData(List<Path> paths, String startTime, String endTime, int bobIndex) {
        int processedPathIndex = 0; // 文件索引
        StringBuilder stringBuilder = new StringBuilder(); // 返回结果
        int totalLine = 0;
        // 创建日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 解析开始和结束时间
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        try {
            startDateTime = LocalDateTime.parse(startTime, formatter);
            endDateTime = LocalDateTime.parse(endTime, formatter);
        } catch (Exception e) {
            // 如果解析失败，尝试使用字符串比较（保留原有逻辑作为后备）
            System.err.println("日期时间解析失败，将使用字符串比较: " + e.getMessage());
        }

        for (int i = 0; i < paths.size(); i++) {
            processedPathIndex = i;
            Path path = paths.get(i);
            String data = CsvMerger.parseCSVWithoutHeaderAsString(path);
            int dataLines = CsvMerger.countLines(data);
            totalLine += dataLines;
            if (totalLine > 3000 && i > 0) {
                break;
            }
            // 分隔为一行一行
            String[] lines = data.split("\n");
            for (String line : lines) {
                // 分隔到某一个字段
                String[] single = line.split(",");
                // 确保索引在范围内
                if (single.length <= bobIndex) {
                    continue;
                }
                // 时间校验
                String startDataTime = single[bobIndex].split("\\+")[0];
                String endDataTime = single[bobIndex + 1].split("\\+")[0];
                LocalDateTime startDateLocalTime = LocalDateTime.parse(startDataTime, formatter);
                LocalDateTime endDateLocalTime = LocalDateTime.parse(endDataTime, formatter);
                if (startDateTime != null && endDateTime != null) {
                    if (!startDateLocalTime.isAfter(endDateTime) && !endDateLocalTime.isBefore(startDateTime)) {
                        stringBuilder.append(line).append("\n");
                    }
                } else {
                    stringBuilder.append(line).append("\n");
                }
            }

        }
        List<Path> remainingPaths = new ArrayList<>();
        for (int i = processedPathIndex + 1; i < paths.size(); i++) {
            remainingPaths.add(paths.get(i));
        }
        return new DataBack(stringBuilder.toString(), remainingPaths);
    }

    //    public DataBack getTickData(List<Path> paths, String startTime, String endTime, int createTimeIndex) {
//        int processedPathIndex = 0; // 文件索引
//        StringBuilder stringBuilder = new StringBuilder(); // 返回结果
//        int totalLine = 0;
//        // 创建日期时间格式化器
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        // 解析开始和结束时间
//        LocalDateTime startDateTime = null;
//        LocalDateTime endDateTime = null;
//
//        try {
//            startDateTime = LocalDateTime.parse(startTime, formatter);
//            endDateTime = LocalDateTime.parse(endTime, formatter);
//        } catch (Exception e) {
//            // 如果解析失败，尝试使用字符串比较（保留原有逻辑作为后备）
//            System.err.println("日期时间解析失败，将使用字符串比较: " + e.getMessage());
//        }
//
//        for (int i = 0; i < paths.size(); i++) {
//            processedPathIndex = i;
//            Path path = paths.get(i);
//            String data = CsvMerger.parseCSVWithoutHeaderAsString(path);
//            int dataLines = CsvMerger.countLines(data);
//            totalLine += dataLines;
//            if (totalLine > 3000 && i > 0) {
//                break;
//            }
//            // 分隔为一行一行
//            String[] lines = data.split("\n");
//            for (String line : lines) {
//                // 分隔到某一个字段
//                String[] single = line.split(",");
//                // 确保索引在范围内
//                if (single.length <= createTimeIndex) {
//                    continue;
//                }
//                // 时间校验
//                String createDataTime = single[createTimeIndex].split("\\+")[0];
//                LocalDateTime createDateLocalTime = LocalDateTime.parse(createDataTime, formatter);
//                if (startDateTime != null && endDateTime != null) {
//                    if (!createDateLocalTime.isAfter(endDateTime) && !createDateLocalTime.isBefore(startDateTime)) {
//                        stringBuilder.append(line).append("\n");
//                    }
//                } else {
//                    stringBuilder.append(line).append("\n");
//                }
//            }
//        }
//        List<Path> remainingPaths = new ArrayList<>();
//        for (int i = processedPathIndex + 1; i < paths.size(); i++) {
//            remainingPaths.add(paths.get(i));
//        }
//        return new DataBack(stringBuilder.toString(), remainingPaths);
//    }
    public DataLabel getTickData(List<Path> paths, String startTime, String endTime, int createTimeIndex, int nextIndex, int count) {
        if (count > 180) {
            count = 180;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int totalLine = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        int index = 0;
        int currentLine = 0;

        // 尝试解析时间范围
        try {
            if (!startTime.isEmpty()) startDateTime = LocalDateTime.parse(startTime, formatter);
            if (!endTime.isEmpty()) endDateTime = LocalDateTime.parse(endTime, formatter);
        } catch (Exception e) {
            System.err.println("日期时间解析失败: " + e.getMessage());
        }

        int processedPathIndex = 0;
        for (int i = 0; i < paths.size(); i++) {
            processedPathIndex = i;
            Path path = paths.get(i);
            currentLine = CsvMerger.countLines(CsvMerger.parseCSVWithoutHeaderAsString(paths.get(i)));

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                // 获取并跳过标题行
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    stringBuilder.append(headerLine).append("\n");
                }

                // 使用更智能的CSV解析
                String line;
                while ((line = reader.readLine()) != null) {
                    // 获取完整行，但需要处理引号中的逗号
                    List<String> fields = parseCSVLine(line);
                    totalLine++;
                    // 确保创建时间索引在范围内
                    if (fields.size() <= createTimeIndex || createTimeIndex < 0) {
                        System.err.println("警告: 创建时间索引超出范围");
                        continue;
                    }
                    // 提取日期时间
                    String createdAtField = fields.get(createTimeIndex);
                    LocalDateTime recordTime = extractDateTime(createdAtField);
                    // 时间筛选
                    if (shouldIncludeRecord(recordTime, startDateTime, endDateTime)) {
                        stringBuilder.append(line).append("\n");
                    }
                    if (totalLine - nextIndex > count && i > 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("读取文件失败: " + e.getMessage());
            }
        }
        // 每一行数据
        String[] allLines = stringBuilder.toString().split("\\r?\\n");
        // 实际限制
        int actualLimit = Math.min(count, allLines.length);
        for (int i = nextIndex; i < actualLimit + nextIndex; i++) {
            stringBuilder.append(allLines[i]);
            if (i < actualLimit + nextIndex - 1) {
                stringBuilder.append("\n");
            }
        }


        if (count < allLines.length) {
            index = nextIndex
                    + actualLimit
                    + currentLine // 当前文件的行数
                    - allLines.length; // 总的读取到的行数
            if (index < 0) {
                int lines = CsvMerger.countLines(CsvMerger.parseCSVWithoutHeaderAsString(paths.get(0)));
                index = lines + index;
            }
        }

        // 构建剩余路径
        List<Path> remainingPaths = new ArrayList<>();
        for (int i = processedPathIndex; i < paths.size(); i++) {
            remainingPaths.add(paths.get(i));
        }

        return new DataLabel(stringBuilder.toString(), index, remainingPaths);
    }

    // 解析CSV行，正确处理引号内的内容
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    // 从字段中提取日期时间
    private LocalDateTime extractDateTime(String createdAtField) {
        // 处理可能的格式，例如："2025-04-28 09:15:00+0800"
        try {
            // 移除可能的前缀
            String dateStr = createdAtField;
            if (dateStr.contains("created_at")) {
                dateStr = dateStr.substring(dateStr.indexOf(":") + 1).trim();
            }

            // 移除时区信息
            if (dateStr.contains("+")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("+"));
            }

            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            System.err.println("提取日期时间失败: " + e.getMessage());
            return null;
        }
    }

    // 检查记录是否在指定的时间范围内
    private boolean shouldIncludeRecord(LocalDateTime recordTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (recordTime == null) return true; // 如果无法解析时间，则包含该记录
        if (startTime == null && endTime == null) return true; // 如果没有指定时间范围，则包含所有记录

        boolean afterStart = startTime == null || !recordTime.isBefore(startTime);
        boolean beforeEnd = endTime == null || !recordTime.isAfter(endTime);

        return afterStart && beforeEnd;
    }


}

