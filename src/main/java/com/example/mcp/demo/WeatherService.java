/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// 上面的注释是版权信息和Apache 2.0开源许可协议，声明了代码的使用规则。
// 就像给代码穿上“法律防护服”，让它无忧无虑地出门！

package com.example.mcp.demo;
// 定义了代码所在的包，这里是“com.example”，便于组织和管理类文件。

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// 导入常用的集合类和流操作工具，方便后续代码中处理集合。

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// 导入Jackson JSON处理库的注解，用于在序列化和反序列化时指定行为。

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
// 导入Spring相关注解和Rest客户端异常处理类，为Spring Boot项目添加注解支持及HTTP请求功能。

@Service
// 使用@Service注解将WeatherService类标识为Spring容器中的服务组件，方便依赖注入。
public class WeatherService {

	private static final String BASE_URL = "https://api.weather.gov";
	// 定义常量BASE_URL，指向天气API的基础地址

	private final RestClient restClient;
	// 声明一个RestClient成员变量，用于发送HTTP请求

	public WeatherService() {
		// WeatherService的构造方法

		this.restClient = RestClient.builder()
				.baseUrl(BASE_URL)
				.defaultHeader("Accept", "application/geo+json")
				.defaultHeader("User-Agent", "WeatherApiClient/1.0 (redoom8@gmail.com)")
				.build();
		// 通过RestClient.builder()构建RestClient实例：
		// - 设置基础URL为weather.gov API的地址；
		// - 默认请求头"Accept"指定返回数据格式为GeoJSON；
		// - 默认请求头"User-Agent"中包含了客户端信息，别忘了用你自己的邮箱哦~
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	// 当JSON中有本类未定义的字段时忽略它们，保证解析时不会抛异常
	public record Points(@JsonProperty("properties") Props properties) {
		// 定义record Points，用于封装从/api/points接口返回的数据
		// 该record包含一个名为properties的成员，其JSON字段名为"properties"

		@JsonIgnoreProperties(ignoreUnknown = true)
		// 同上，忽略未知的JSON属性12
		public record Props(@JsonProperty("forecast") String forecast) {
			// 内部record Props，其包含一个forecast字段（字符串类型），用于存放天气预报的URL
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	// 同上，忽略JSON中不需要的字段
	public record Forecast(@JsonProperty("properties") Props properties) {
		// 定义record Forecast，用于封装预报数据，包含一个属性Properties

		@JsonIgnoreProperties(ignoreUnknown = true)
		// 忽略不认识的JSON属性
		public record Props(@JsonProperty("periods") List<Period> periods) {
			// 内部record Props，其包含一个List类型的periods字段，存放多个时间段的天气预报数据
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		// 同上，忽略未知字段
		public record Period(@JsonProperty("number") Integer number,
							 @JsonProperty("name") String name,
							 @JsonProperty("startTime") String startTime,
							 @JsonProperty("endTime") String endTime,
							 @JsonProperty("isDaytime") Boolean isDayTime,
							 @JsonProperty("temperature") Integer temperature,
							 @JsonProperty("temperatureUnit") String temperatureUnit,
							 @JsonProperty("temperatureTrend") String temperatureTrend,
							 @JsonProperty("probabilityOfPrecipitation") Map probabilityOfPrecipitation,
							 @JsonProperty("windSpeed") String windSpeed,
							 @JsonProperty("windDirection") String windDirection,
							 @JsonProperty("icon") String icon,
							 @JsonProperty("shortForecast") String shortForecast,
							 @JsonProperty("detailedForecast") String detailedForecast) {
			// 内部record Period，包含了具体的天气时段信息：
			// - 例如时段编号、名称、开始和结束时间、是否为白天、温度信息、降水概率、风速及风向、图标及预报摘要等等。
			// 这就是天气“流水账”，每条都详尽无遗，就像天气预报员认真播报每分钟的变化。
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	// 忽略未知JSON属性
	public record Alert(@JsonProperty("features") List<Feature> features) {
		// 定义record Alert，用于封装天气警报数据，其中features字段为一个Feature列表

		@JsonIgnoreProperties(ignoreUnknown = true)
		// 忽略未知字段
		public record Feature(@JsonProperty("properties") Properties properties) {
			// 内部record Feature，包含了一个Properties对象，用于存放警报具体内容
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		// 忽略不相关的JSON字段
		public record Properties(@JsonProperty("event") String event,
								 @JsonProperty("areaDesc") String areaDesc,
								 @JsonProperty("severity") String severity,
								 @JsonProperty("description") String description,
								 @JsonProperty("instruction") String instruction) {
			// 内部record Properties，记录了天气警报的各个细节：
			// - 事件名称、影响区域、严重程度、详细描述和应对指南等。
			// 警报来了，就像有坏蛋要闹腾似的，提醒我们注意安全！
		}
	}

	/**
	 * Get forecast for a specific latitude/longitude
	 * @param latitude Latitude
	 * @param longitude Longitude
	 * @return The forecast for the given location
	 * @throws RestClientException if the request fails
	 */
	@Tool(description = "Get weather forecast for a specific latitude/longitude")
	// 使用@Tool注解标记此方法，可以供其他工具或系统调用，描述信息为：获取特定经纬度的天气预报
	public String getWeatherForecastByLocation(double latitude, double longitude) {

		var points = restClient.get()
				.uri("/points/{latitude},{longitude}", latitude, longitude)
				.retrieve()
				.body(Points.class);
		// 通过RestClient发送GET请求，访问"/points/{latitude},{longitude}"接口，将结果解析为Points对象
		// 这一步就像拿到了一张通往天气世界的门票。

		var forecast = restClient.get()
				.uri(points.properties().forecast())
				.retrieve()
				.body(Forecast.class);
		// 从刚刚拿到的Points对象中获取forecast字段（接口URL），再次调用GET请求获取详细的天气预报数据，并解析成Forecast对象
		// 再次申请“VIP门票”，进入更深入的预报信息区域。

		String forecastText = forecast.properties().periods().stream().map(p -> {
			// 使用Java Stream API对所有预报时段进行处理
			return String.format("""
					%s:
					Temperature: %s %s
					Wind: %s %s
					Forecast: %s
					""", p.name(), p.temperature(), p.temperatureUnit(), p.windSpeed(), p.windDirection(),
					p.detailedForecast());
			// 对每个时段格式化字符串：显示名称、温度、单位、风速、风向以及详细预报
			// 就像天气小报，每一行都力求准确又详细，让你不错过任何一个细节！
		}).collect(Collectors.joining());
		// 将所有时段拼接成一个完整的预报文本

		return forecastText;
		// 返回拼接后的天气预报信息字符串
	}

	/**
	 * Get alerts for a specific area
	 * @param state Area code. Two-letter US state code (e.g. CA, NY)
	 * @return Human readable alert information
	 * @throws RestClientException if the request fails
	 */
	@Tool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
	// 使用@Tool注解标记此方法，可以供工具调用，描述信息为：获取指定美国州的天气警报，输入参数为2位字母的州代码
	public String getAlerts(String state) {
		Alert alert = restClient.get()
				.uri("/alerts/active/area/{state}", state)
				.retrieve()
				.body(Alert.class);
		// 通过RestClient发送GET请求，访问活跃警报接口"/alerts/active/area/{state}"，并解析返回的JSON为Alert对象
		// 拿到警报数据，就像收到了天气“闹钟”响起的提醒！

		return alert.features()
				.stream()
				.map(f -> String.format("""
					Event: %s
					Area: %s
					Severity: %s
					Description: %s
					Instructions: %s
					""", f.properties().event(), f.properties.areaDesc(), f.properties.severity(),
						f.properties.description(), f.properties.instruction()))
				.collect(Collectors.joining("\n"));
		// 遍历所有警报信息，格式化成易读的字符串形式，并用换行符拼接起来
		// 这样一来，所有警报就整齐地排成了一队，方便用户阅读和采取行动，就像电影院排队买票一样 orderly！
	}

	public static void main(String[] args) {
		WeatherService client = new WeatherService();
		// 在main方法中创建WeatherService实例
		System.out.println(client.getWeatherForecastByLocation(47.6062, -122.3321));
		// 调用获取天气预报的方法，并打印结果，此处传入了西雅图的经纬度（47.6062, -122.3321）
		System.out.println(client.getAlerts("NY"));
		// 调用获取警报的方法，并打印结果，此处传入州代码“NY”，意思是我们要看看纽约是否有“骚动”
	}
}
