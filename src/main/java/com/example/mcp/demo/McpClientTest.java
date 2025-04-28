package com.example.mcp.demo;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

public class McpClientTest {
    public static void main(String[] args) {
        // 配置连接到服务器的参数
        // 在实际环境中，这应该指向你的服务器进程
        ServerParameters params = ServerParameters.builder("java")
            .args("-cp", "path/to/classpath", "McpStdioServerExample")
            .build();
        
        // 创建 STDIO 客户端传输
        StdioClientTransport transport = new StdioClientTransport(params);
        
        // 创建同步客户端
        var client = McpClient.sync(transport).build();
        
        try {
            // 初始化连接
            client.initialize();
            
            // 列出可用的资源
            var resources = client.listResources();
            System.out.println("可用资源: " + resources.resources());
            
            // 读取特定资源
            if (!resources.resources().isEmpty()) {
                Resource firstResource = resources.resources().get(0);
                ReadResourceResult result = client.readResource(firstResource);
                
                // 处理结果
                if (!result.contents().isEmpty() && result.contents().get(0) instanceof TextResourceContents) {
                    TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
                    System.out.println("资源内容: " + textContent.text());
                }
            }
        } 
        finally {
            // 关闭客户端
            client.close();
        }
    }
}