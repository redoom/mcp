package com.example;

// 导入Spring AI和Spring Boot所需的类
import com.example.mcp.demo.WeatherService;
import com.example.mcp.tool.Vvtr;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SpringBootApplication
public class McpServerApplication {

	private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

	public static void main(String[] args) {
		// 启动Spring Boot应用
		SpringApplication.run(McpServerApplication.class, args);
	}

	// 定义一个Bean，返回ToolCallbackProvider实例，用于注册工具回调
	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		// 使用MethodToolCallbackProvider来注册WeatherService实例作为工具对象
		return MethodToolCallbackProvider.builder()
				.toolObjects(weatherService) // 注册WeatherService作为工具对象
				.build(); // 构建并返回ToolCallbackProvider实例
	}

	// 定义一个记录类型TextInput，表示输入的文本
	public record TextInput(String input) {
	}

	// 定义一个Bean，返回ToolCallback实例，用于将文本转换为大写
	@Bean
	public ToolCallback toUpperCase() {
		// 使用FunctionToolCallback来实现将文本转换为大写的功能
		return FunctionToolCallback.builder("toUpperCase", (TextInput input) -> input.input().toUpperCase())
				// 设置函数名称为"toUpperCase"
				.inputType(TextInput.class) // 设置输入类型为TextInput
				.description("Put the text to upper case") // 添加功能描述
				.build(); // 构建并返回ToolCallback实例
	}


	@Bean
	public ToolCallbackProvider vvtrTools(Vvtr vvtrDate) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(vvtrDate)
				.build();
	}

//	@Bean
//	public List<McpServerFeatures.SyncResourceSpecification> resources() throws IOException {
//		List<McpServerFeatures.SyncResourceSpecification> resourceSpecifications = new ArrayList<>();
//		List<Path> allCsvFiles = CsvMerger.findAllCsvFiles(CsvMerger.ROOT);
//		for (Path csvFile : allCsvFiles) {
//			// 使用标准化的URI格式而不是文件路径
//			String fileName = csvFile.getFileName().toString();
//			String resourceUri = "file:///" + fileName;
//
//			var systemInfoResource = new McpSchema.Resource(
//					resourceUri,  // 使用更标准的URI格式
//					UUID.randomUUID().toString(),
//					"加密货币资源: " + fileName,  // 添加文件名以便区分
//					"text/csv",
//					null
//			);
//			var csvData = new McpServerFeatures.SyncResourceSpecification(
//					systemInfoResource,
//					(exchange, request) -> {
//						try {
//							String csvContent = Files.readString(csvFile);
//							// 添加日志以确认资源读取成功
//							System.out.println("Successfully read CSV file: " + csvFile);
//							return new McpSchema.ReadResourceResult(
//									List.of(new McpSchema.TextResourceContents(request.uri(), "text/csv", csvContent))
//							);
//						} catch (IOException e) {
//							System.err.println("Failed to read CSV file: " + csvFile);
//							e.printStackTrace();
//							throw new RuntimeException("Failed to read CSV file: " + csvFile, e);
//						}
//					}
//			);
//			resourceSpecifications.add(csvData);
//			// 添加日志以确认资源注册
//			System.out.println("Registered resource: " + resourceUri);
//		}
//		return resourceSpecifications;
//	}
//
	@Bean
	public List<McpServerFeatures.SyncResourceSpecification> resources() throws IOException {
		String resourceUri = "file:///" + "C:\\Users\\Administrator\\Desktop\\csv_files.txt";
		var systemInfoResource = new McpSchema.Resource(resourceUri,
				UUID.randomUUID().toString(),
				"加密货币资源",
				"text/plain",
				null);
		var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
			try {
				String csvContent = Files.readString(Path.of(resourceUri));
				return new McpSchema.ReadResourceResult(
						List.of(new McpSchema.TextResourceContents(request.uri(), "text/plain", csvContent))
				);
			} catch (IOException e) {
				// 处理异常
				throw new RuntimeException("Failed to read CSV file", e);
			}
		});

		return List.of(resourceSpecification);
	}

	@Bean
	public List<McpServerFeatures.SyncResourceSpecification> resourcesTest() throws IOException {
		String target = "https://s1.hdslb.com/bfs/static/jinkela/popular/assets/icon_weekly.png";
		var systemInfoResource = new McpSchema.Resource(target,
				UUID.randomUUID().toString(),
				"图片资源",
				"image/png",
				null);
		var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
			try {
				var systemInfo = Map.of("test", "test");
				String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
				return new McpSchema.ReadResourceResult(
						List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to generate system info", e);
			}
		});
		return List.of(resourceSpecification);
	}

}
