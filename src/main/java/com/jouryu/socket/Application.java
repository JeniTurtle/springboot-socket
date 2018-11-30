package com.jouryu.socket;

import com.jouryu.socket.netty.RunNetty;
import com.jouryu.socket.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		SpringUtil springUtil = new SpringUtil();
		springUtil.setApplicationContext(context);
		SpringUtil.getBean(RunNetty.class).run();
	}
}
