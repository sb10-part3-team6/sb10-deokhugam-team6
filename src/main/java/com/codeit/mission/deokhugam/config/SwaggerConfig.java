package com.codeit.mission.deokhugam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(apiInfo())
        // Deokhugam-Request-User-ID 대응
        .addSecurityItem(new SecurityRequirement().addList("User-ID-Header"))
        .components(new Components()
            .addSecuritySchemes("User-ID-Header", new SecurityScheme()
                .name("Deokhugam-Request-User-ID")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("사용자 식별을 위한 UUID 헤더를 입력해주세요.")));
  }

  private Info apiInfo() {
    return new Info()
        .title("덕후감 (Deokhugam) API 명세서")
        .description("책 읽는 즐거움을 공유하고, 지식과 감상을 나누는 책 덕후들의 커뮤니티 서비스 '덕후감'의 백엔드 API 문서입니다.")
        .version("v1.0.0")
        .contact(new Contact()
            .name("Deokhugam Team 6")
            .url("https://github.com/sb10-part3-team6/sb10-deokhugam-team6"))
        .license(new io.swagger.v3.oas.models.info.License()
            .name("Apache 2.0")
            .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
  }
}