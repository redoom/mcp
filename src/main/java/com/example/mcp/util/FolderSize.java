package com.example.mcp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FolderSize {
    public static void main(String[] args) {
        // 指定要计算大小的文件夹路径
        String folderPath = "C:\\Program Files\\code\\PythonProject\\output\\20250108";
        
        // 方法二：使用NIO和Stream API (Java 8+)
        try {
            Path path = Paths.get(folderPath);
//            long count = getDataCount(path);
            System.out.println("一共有: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 使用File类递归计算文件夹大小
    public static long getFolderSizeFile(File folder) {
        long size = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getFolderSizeFile(file); // 递归计算子文件夹
                    }
                }
            }
        } else {
            size = folder.length();
        }
        return size;
    }
    
    // 使用NIO和Stream API计算文件夹大小
    public static long getFolderSizeNIO(Path path) throws Exception {
        // 使用try-with-resources自动关闭Stream资源，避免内存泄漏
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                    // 筛选出所有常规文件，忽略目录和符号链接等
                    .filter(Files::isRegularFile)
                    // 将Path对象转换为文件大小（长整型）
                    .mapToLong(p -> {
                        try {
                            // 获取单个文件的大小（字节）
                            return Files.size(p);
                        } catch (Exception e) {
                            // 如果无法访问某个文件（如权限问题），返回0不影响总计算
                            return 0L;
                        }
                    })
                    // 对所有文件大小求和，得到总大小
                    .sum();
        }
        // try-with-resources会自动关闭Stream，确保资源释放
    }
    
    // 格式化文件大小为可读形式
    public static String formatSize(long size) {
        String[] units = {"字节", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        
        while (fileSize > 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    public static Long getDataCount(long size) {
        return size / 120;
    }

    public static Long getFileSize(List<Path> paths) {
        long sum = 0L;

        for (Path path : paths) {
            try {
                if (Files.isRegularFile(path)) {
                    long size = Files.size(path);
                    sum += size;
                }
            } catch (IOException e) {
                // Handle exceptions appropriately
                System.err.println("Error getting size for: " + path + " - " + e.getMessage());
            }
        }

        return sum;
    }

}