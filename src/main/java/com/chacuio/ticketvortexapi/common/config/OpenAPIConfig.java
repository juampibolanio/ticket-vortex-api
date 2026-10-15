package com.chacuio.ticketvortexapi.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Ticket Vortex Web API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Juan Pablo Bolanio", email = "juampi360786@gmail.com", url = "https://portfolio-juampi-bolanio.vercel.app/"
                ),
                license = @License(
                        name = "Apache 2.0", url = "https://apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Production"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        bearerFormat = "bearer",
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.HTTP,
        description = "Authentication with JWT",
        scheme = "bearer"
)
@Configuration
public class OpenAPIConfig {
}
