package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SpringbootApplication {
	
	private static Logger log = LoggerFactory.getLogger(SpringbootApplication.class);

	public static void main(String[] args){
		SpringApplication.run(SpringbootApplication.class, args);
		
		log.info("[springboot]已初始化完毕，服务已开启，正在等待定时任务执行...");
	}
}
