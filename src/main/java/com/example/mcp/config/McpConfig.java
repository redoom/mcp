//package com.example.mcp.config;
//
//import io.modelcontextprotocol.client.McpSyncClient;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.tool.annotation.Tool;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Map;
//
//@Configuration
//public class McpConfig {
//
//    @Bean(destroyMethod = "close")
//    public McpSyncClient mcpClient() {
//        // 初始化 MCP 客户端（示例）…
//    }
//
//    @Bean
//    public McpFunctionCallback parseSegmentCallback(McpSyncClient mcpClient) {
//        // 定义工具元数据：参数为 lines: array[string]
//        Tool tool = new Tool(
//                "parseCsvSegment",
//                "解析单个 CSV 文本段为 KLineData 数组",
//                Map.of("lines", "array[string]")
//        );
//        return new McpFunctionCallback(mcpClient, tool);
//    }
//
//    @Bean
//    public ChatClient chatClient(List<McpFunctionCallback> callbacks) {
//        return ChatClient.builder()
//                .defaultFunctions(callbacks)
//                .build();
//    }
//}
