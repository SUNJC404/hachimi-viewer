package tv.oxnu0xuu.hachimiviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HachimiViewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HachimiViewerApplication.class, args);
	}

}
