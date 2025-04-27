package com.example;

// 导入Spring AI和Spring Boot所需的类
import com.example.mcp.tool.VvtrDate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication // 标记该类为Spring Boot应用的入口
public class McpServerApplication {

	public static void main(String[] args) {
		// 启动Spring Boot应用
		SpringApplication.run(McpServerApplication.class, args);
	}

//	// 定义一个Bean，返回ToolCallbackProvider实例，用于注册工具回调
//	@Bean
//	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
//		// 使用MethodToolCallbackProvider来注册WeatherService实例作为工具对象
//		return MethodToolCallbackProvider.builder()
//				.toolObjects(weatherService) // 注册WeatherService作为工具对象
//				.build(); // 构建并返回ToolCallbackProvider实例
//	}
//
//	// 定义一个记录类型TextInput，表示输入的文本
//	public record TextInput(String input) {
//	}
//
//	// 定义一个Bean，返回ToolCallback实例，用于将文本转换为大写
//	@Bean
//	public ToolCallback toUpperCase() {
//		// 使用FunctionToolCallback来实现将文本转换为大写的功能
//		return FunctionToolCallback.builder("toUpperCase", (TextInput input) -> input.input().toUpperCase())
//				// 设置函数名称为"toUpperCase"
//				.inputType(TextInput.class) // 设置输入类型为TextInput
//				.description("Put the text to upper case") // 添加功能描述
//				.build(); // 构建并返回ToolCallback实例
//	}
//
//
//	@Bean
//	public ToolCallbackProvider vvtrTools(VvtrDate vvtrDate) {
//		return MethodToolCallbackProvider.builder()
//				.toolObjects(vvtrDate)
//				.build();
//	}

}
