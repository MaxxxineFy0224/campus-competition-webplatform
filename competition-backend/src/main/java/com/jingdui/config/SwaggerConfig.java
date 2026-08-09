package com.jingdui.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置
 *
 * 访问地址：http://localhost:8080/swagger-ui.html
 * JSON 文档：http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园竞赛组队平台 API")
                        .version("1.0.0")
                        .description("校园竞赛组队平台后端接口文档，涵盖竞赛、组队帖、用户认证、AI 聊天等模块。")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@jingdui.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // 全局 JWT Bearer 认证
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("在登录/注册接口获取 token 后，在此填入 Bearer token"));
    }
}