package com.jouryu;

import com.jouryu.netty.RunNetty;
import com.jouryu.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
		SpringUtil.getBean("runNetty", RunNetty.class).run();
	}
}
