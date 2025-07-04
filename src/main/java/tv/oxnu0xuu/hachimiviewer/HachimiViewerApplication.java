package tv.oxnu0xuu.hachimiviewer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;

@SpringBootApplication
@EnableScheduling
@MapperScan("tv.oxnu0xuu.hachimiviewer.mapper")
public class HachimiViewerApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(HachimiViewerApplication.class);

		// 以编程方式设置默认属性
		// 这会覆盖 application.properties 中的同名属性
		// 也可以从环境变量、数据库或其他来源动态获取
		String port = System.getenv("SERVER_PORT");
		if (port == null || port.isEmpty()) {
			port = "8080"; // 默认端口
		}
		app.setDefaultProperties(Collections.singletonMap("server.port", port));

		app.run(args);
	}

}