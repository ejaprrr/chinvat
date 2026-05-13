package eu.alboranplus.chinvat.eidas.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EidasProperties.class)
public class EidasConfiguration {

	@Bean
	@ConditionalOnMissingBean(ObjectMapper.class)
	public ObjectMapper eidasObjectMapper() {
		return new ObjectMapper().findAndRegisterModules();
	}

	@Bean
	public HttpClient eidasHttpClient(EidasProperties eidasProperties) {
		return HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(eidasProperties.getBrokerConnectTimeoutMillis()))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}
}
