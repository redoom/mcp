package com.example.mcp.startup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;


@Component
public class FolderInitializer implements ApplicationRunner {

    @Value("${file.base-path}")
    private String basePath;

    /**
     * 启动时创建所需文件夹
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        List<String> folders = Arrays.asList(
                "A-shares",         // A股
                "Futures",          // 期货
                "Funds",            // 基金
                "Indices",          // 指数
                "US-Stocks",        // 美股
                "Options",          // 美股期权
                "Crypto"            // 加密币
        );
        List<String> type = Arrays.asList(
                "1d", "1m", "15m", "tick"
        );

        for (String folderName : folders) {
            File dir = new File(basePath, folderName);
            for (String typeName : type) {
                File typeDir = new File(dir, typeName);
                if (!typeDir.exists()) {
                    boolean created = typeDir.mkdirs();
                    if (created) {
                        System.out.println("Created folder: " + typeDir.getAbsolutePath());
                    } else {
                        System.err.println("Failed to create folder: " + typeDir.getAbsolutePath());
                    }
                } else {
                    System.out.println("Folder already exists: " + typeDir.getAbsolutePath());
                }
            }
        }
    }
}
