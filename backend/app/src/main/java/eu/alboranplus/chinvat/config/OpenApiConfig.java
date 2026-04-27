package eu.alboranplus.chinvat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI chinvatOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Chinvat API")
                .description(
                    "Chinvat Málaga 2026 — stateless REST backend.\n\n"
                        + "Protected endpoints require a bearer token obtained from `POST /api/v1/auth/login`. "
                        + "Pass it in the `Authorization: Bearer <token>` header.")
                .version("0.0.1-SNAPSHOT")
                .contact(
                    new Contact()
                        .name("Alborán Plus")
                        .email("dev@alboranplus.eu")
                        .url("https://alboranplus.eu"))
                .license(new License().name("Proprietary").url("https://alboranplus.eu")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("opaque")
                        .description(
                            "Opaque bearer token (BASE64URL-encoded). "
                                + "Obtain one via POST /api/v1/auth/login.")));
  }
}
