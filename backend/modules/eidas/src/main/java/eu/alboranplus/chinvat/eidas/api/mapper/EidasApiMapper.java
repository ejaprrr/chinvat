package eu.alboranplus.chinvat.eidas.api.mapper;

import eu.alboranplus.chinvat.eidas.api.dto.EidasCallbackRequest;
import eu.alboranplus.chinvat.eidas.api.dto.EidasCallbackResponse;
import eu.alboranplus.chinvat.eidas.api.dto.EidasLoginRequest;
import eu.alboranplus.chinvat.eidas.api.dto.EidasLoginResponse;
import eu.alboranplus.chinvat.eidas.api.dto.EidasProviderResponse;
import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.command.InitiateEidasLoginCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import org.springframework.stereotype.Component;

@Component
public class EidasApiMapper {

  public InitiateEidasLoginCommand toCommand(EidasLoginRequest request) {
    return new InitiateEidasLoginCommand(request.providerCode(), request.redirectUri());
  }

  public HandleEidasCallbackCommand toCommand(EidasCallbackRequest request) {
    return new HandleEidasCallbackCommand(
        request.providerCode(),
        request.state(),
        request.authorizationCode(),
        request.externalSubjectId(),
        request.levelOfAssurance());
  }

  public EidasLoginResponse toResponse(EidasLoginView view) {
    return new EidasLoginResponse(view.providerCode(), view.state(), view.authorizationUrl(), view.expiresAt());
  }

  public EidasCallbackResponse toResponse(EidasCallbackView view) {
    return new EidasCallbackResponse(
        view.providerCode(),
        view.externalSubjectId(),
        view.levelOfAssurance(),
      view.currentStatus(),
        view.linkedUserId(),
        view.profileCompletionRequired(),
        view.processedAt());
  }

  public EidasProviderResponse toResponse(EidasProviderView view) {
    return new EidasProviderResponse(view.code(), view.displayName(), view.countryCode(), view.enabled());
  }
}
