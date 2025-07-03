package tv.oxnu0xuu.hachimiviewer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("tv.oxnu0xuu.hachimiviewer.mapper")
public class HachimiViewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HachimiViewerApplication.class, args);
	}

}
