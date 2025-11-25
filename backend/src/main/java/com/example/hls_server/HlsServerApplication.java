package com.example.hls_server;

import com.example.hls_server.gui.ServerUI;
import com.example.hls_server.service.ClientMonitor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class HlsServerApplication {

	public static void main(String[] args) {
		// Enable GUI support - disable headless mode
		System.setProperty("java.awt.headless", "false");

		ConfigurableApplicationContext context = SpringApplication.run(HlsServerApplication.class, args);

		// Launch GUI after Spring Boot starts (if enabled)
		Environment env = context.getEnvironment();
		boolean guiEnabled = env.getProperty("server.gui.enabled", Boolean.class, true);

		if (guiEnabled) {
			ClientMonitor clientMonitor = context.getBean(ClientMonitor.class);
			ServerUI.showUI(clientMonitor);
		} else {
			System.out.println("Server GUI is disabled (server.gui.enabled=false)");
		}
	}

}
