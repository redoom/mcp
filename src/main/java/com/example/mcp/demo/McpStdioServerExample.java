package com.example.mcp.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

import java.util.List;

public class McpStdioServerExample {
    public static void main(String[] args) {
        // 创建 STDIO 传输提供者
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider();

        // 创建资源规范
        McpServerFeatures.SyncResourceSpecification resourceSpec = new McpServerFeatures.SyncResourceSpecification(
            new Resource(
                "custom://data",         // 资源的 URI
                "示例数据",               // 资源的名称
                "这是一个示例数据资源",    // 资源的描述
                "text/plain",            // MIME 类型
                null                     // 注解（可选）
            ),
            (exchange, request) -> {
                // 这里实现资源读取逻辑
                // 在实际应用中，可能是从文件、数据库或API读取数据
                String content = "这是从服务器返回的示例数据内容";
                
                // 创建文本资源内容
                TextResourceContents resourceContents = new TextResourceContents(
                    request.uri(),       // 使用请求中的 URI
                    "text/plain",        // MIME 类型
                    content              // 实际内容
                );
                
                // 返回资源读取结果
                return new ReadResourceResult(List.of(resourceContents));
            }
        );

        // 创建并启动服务器
        var server = McpServer.sync(transportProvider)
            .serverInfo("示例MCP服务器", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .resources(true, false)  // 启用资源功能，但不启用订阅
                .build())
            .resources(resourceSpec)     // 添加我们的资源
            .build();

        // 服务器现在在标准输入/输出上运行
        System.err.println("MCP 服务器已启动，通过 STDIO 通信...");
        
        // 保持主线程运行
        // 在实际应用中，你可能会等待某个条件来优雅地关闭服务器
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            server.closeGracefully();
        }
    }
}