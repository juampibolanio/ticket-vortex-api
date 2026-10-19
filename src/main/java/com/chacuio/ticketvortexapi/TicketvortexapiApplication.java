package com.chacuio.ticketvortexapi;

import com.chacuio.ticketvortexapi.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TicketvortexapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketvortexapiApplication.class, args);
	}

}
