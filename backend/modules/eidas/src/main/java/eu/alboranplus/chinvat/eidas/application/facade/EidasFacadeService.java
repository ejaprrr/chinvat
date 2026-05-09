package eu.alboranplus.chinvat.eidas.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.eidas.application.command.CompleteEidasProfileCommand;
import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.command.InitiateEidasLoginCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProfileCompletionView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.usecase.CompleteEidasProfileUseCase;
import eu.alboranplus.chinvat.eidas.application.usecase.HandleEidasCallbackUseCase;
import eu.alboranplus.chinvat.eidas.application.usecase.InitiateEidasLoginUseCase;
import eu.alboranplus.chinvat.eidas.application.usecase.ListEidasProvidersUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EidasFacadeService implements EidasFacade {

  private final InitiateEidasLoginUseCase initiateEidasLoginUseCase;
  private final HandleEidasCallbackUseCase handleEidasCallbackUseCase;
  private final CompleteEidasProfileUseCase completeEidasProfileUseCase;
  private final ListEidasProvidersUseCase listEidasProvidersUseCase;
  private final AuditFacade auditFacade;

  public EidasFacadeService(
      InitiateEidasLoginUseCase initiateEidasLoginUseCase,
      HandleEidasCallbackUseCase handleEidasCallbackUseCase,
      CompleteEidasProfileUseCase completeEidasProfileUseCase,
      ListEidasProvidersUseCase listEidasProvidersUseCase,
      AuditFacade auditFacade) {
    this.initiateEidasLoginUseCase = initiateEidasLoginUseCase;
    this.handleEidasCallbackUseCase = handleEidasCallbackUseCase;
    this.completeEidasProfileUseCase = completeEidasProfileUseCase;
    this.listEidasProvidersUseCase = listEidasProvidersUseCase;
    this.auditFacade = auditFacade;
  }

  @Override
  public EidasLoginView initiateLogin(InitiateEidasLoginCommand command) {
    EidasLoginView view = initiateEidasLoginUseCase.execute(command);
    auditFacade.log(
        "EIDAS_LOGIN_INITIATED",
        "anonymous",
        null,
        AuditDetails.builder()
            .add("providerCode", view.providerCode())
            .add("state", view.state())
            .add("expiresAt", view.expiresAt().toString())
            .add("authorizationUrl", view.authorizationUrl())
            .build());
    return view;
  }

  @Override
  public EidasCallbackView handleCallback(HandleEidasCallbackCommand command) {
    EidasCallbackView view = handleEidasCallbackUseCase.execute(command);
    auditFacade.log(
        "EIDAS_CALLBACK_PROCESSED",
        "anonymous",
        view.linkedUserId(),
        AuditDetails.builder()
            .add("providerCode", view.providerCode())
            .add("externalSubjectId", view.externalSubjectId())
            .add("levelOfAssurance", view.levelOfAssurance())
            .add("profileCompletionRequired", view.profileCompletionRequired())
            .add("processedAt", view.processedAt().toString())
            .build());
    return view;
  }

  @Override
  public EidasProfileCompletionView completeProfile(CompleteEidasProfileCommand command, String actor) {
    EidasProfileCompletionView view = completeEidasProfileUseCase.execute(command);
    auditFacade.log(
        "EIDAS_PROFILE_COMPLETED",
        actor,
        view.userId(),
        AuditDetails.builder()
            .add("providerCode", view.providerCode())
            .add("externalSubjectId", view.externalSubjectId())
            .add("currentStatus", view.currentStatus())
            .add("linkedAt", view.linkedAt() == null ? null : view.linkedAt().toString())
            .build());
    return view;
  }

  @Override
  public List<EidasProviderView> listProviders() {
    return listEidasProvidersUseCase.execute();
  }
}
