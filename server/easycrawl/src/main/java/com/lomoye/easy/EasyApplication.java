package com.lomoye.easy;

import com.lomoye.easy.component.DownloaderFactory;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@Slf4j
@SpringBootApplication
@MapperScan("com.lomoye.easy.dao")
public class EasyApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			//注册个钩子，回收一些无法自动回收的资源
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					DownloaderFactory.close();
					log.info("close resource success");
				} catch (IOException e) {
					log.error("close downloader failed", e);
				}
			}));
			ctx.getBean(JobEntry.class).start();
		};
	}
}
