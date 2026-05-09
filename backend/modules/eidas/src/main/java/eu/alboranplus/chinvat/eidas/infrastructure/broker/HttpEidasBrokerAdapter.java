package eu.alboranplus.chinvat.eidas.infrastructure.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasBrokerException;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerCallbackRequest;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerCallbackResponse;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerLoginRequest;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerLoginResponse;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerProviderResponse;
import eu.alboranplus.chinvat.eidas.infrastructure.config.EidasProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("!dev & !local")
@Repository
public class HttpEidasBrokerAdapter implements EidasBrokerPort {

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final EidasProperties eidasProperties;

  public HttpEidasBrokerAdapter(
      HttpClient httpClient, ObjectMapper objectMapper, EidasProperties eidasProperties) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.eidasProperties = eidasProperties;
  }

  @Override
  public List<EidasProviderView> listProviders() {
    BrokerProviderResponse[] response =
        getJson(eidasProperties.getBrokerProvidersPath(), BrokerProviderResponse[].class);
    return java.util.Arrays.stream(response == null ? new BrokerProviderResponse[0] : response)
        .map(
            provider ->
                new EidasProviderView(
                    provider.code(), provider.displayName(), provider.countryCode(), provider.enabled()))
        .toList();
  }

  @Override
  public Optional<EidasProviderView> findEnabledProvider(String providerCode) {
    if (providerCode == null || providerCode.isBlank()) {
      return Optional.empty();
    }
    String normalized = providerCode.toUpperCase(Locale.ROOT);
    return listProviders().stream()
        .filter(EidasProviderView::enabled)
        .filter(provider -> provider.code().toUpperCase(Locale.ROOT).equals(normalized))
        .findFirst();
  }

  @Override
  public EidasLoginView initiateLogin(
      String providerCode, String redirectUri, String state, Instant expiresAt) {
    BrokerLoginResponse response =
        postJson(
            eidasProperties.getBrokerLoginPath(),
        new BrokerLoginRequest(providerCode, redirectUri, state, expiresAt.toString()),
            BrokerLoginResponse.class);
    return new EidasLoginView(
        providerCode,
        state,
        response.authorizationUrl(),
      response.expiresAt() == null ? expiresAt : Instant.parse(response.expiresAt()));
  }

  @Override
  public EidasBrokerIdentityView exchangeAuthorizationCode(
      String providerCode, String state, String authorizationCode) {
    BrokerCallbackResponse response =
        postJson(
            eidasProperties.getBrokerCallbackPath(),
            new BrokerCallbackRequest(providerCode, state, authorizationCode),
            BrokerCallbackResponse.class);
    return new EidasBrokerIdentityView(
        response.externalSubjectId(),
        response.levelOfAssurance(),
        response.personIdentifier(),
        response.legalPersonIdentifier(),
        response.firstName(),
        response.familyName(),
        response.dateOfBirth());
  }

  private <T> T getJson(String path, Class<T> responseType) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder(uri(path)).GET().header("Accept", "application/json").build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new EidasBrokerException(
            "eIDAS broker GET " + path + " failed with HTTP " + response.statusCode() + ": " + response.body());
      }
      return objectMapper.readValue(response.body(), responseType);
    } catch (IOException exception) {
      throw new EidasBrokerException("eIDAS broker request failed", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new EidasBrokerException("eIDAS broker request interrupted", exception);
    }
  }

  private <T> T postJson(String path, Object payload, Class<T> responseType) {
    try {
      String body = objectMapper.writeValueAsString(payload);
      HttpRequest request =
          HttpRequest.newBuilder(uri(path))
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new EidasBrokerException(
            "eIDAS broker POST " + path + " failed with HTTP " + response.statusCode() + ": " + response.body());
      }
      return objectMapper.readValue(response.body(), responseType);
    } catch (IOException exception) {
      throw new EidasBrokerException("eIDAS broker request failed", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new EidasBrokerException("eIDAS broker request interrupted", exception);
    }
  }

  private URI uri(String path) {
    String normalizedBase = eidasProperties.getBrokerBaseUrl().replaceAll("/+$", "");
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    return URI.create(normalizedBase + normalizedPath);
  }
}