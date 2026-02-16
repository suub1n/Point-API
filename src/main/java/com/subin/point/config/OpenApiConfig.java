package com.subin.point.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Point API",
                version = "v1",
                description = "회원 및 포인트 적립/사용/취소 API 문서"
        ),
        servers = {
                @Server(url = "/", description = "Default Server")
        }
)
public class OpenApiConfig {
}
