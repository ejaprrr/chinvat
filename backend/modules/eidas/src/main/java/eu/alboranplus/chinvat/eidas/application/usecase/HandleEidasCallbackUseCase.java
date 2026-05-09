package eu.alboranplus.chinvat.eidas.application.usecase;

import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import eu.alboranplus.chinvat.eidas.application.port.out.ExternalIdentityLifecyclePort;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasInvalidStateException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HandleEidasCallbackUseCase {

  private final EidasBrokerPort eidasBrokerPort;
  private final EidasStatePort eidasStatePort;
  private final ExternalIdentityLifecyclePort externalIdentityLifecyclePort;

  public HandleEidasCallbackUseCase(
      EidasBrokerPort eidasBrokerPort,
      EidasStatePort eidasStatePort,
      ExternalIdentityLifecyclePort externalIdentityLifecyclePort) {
    this.eidasBrokerPort = eidasBrokerPort;
    this.eidasStatePort = eidasStatePort;
    this.externalIdentityLifecyclePort = externalIdentityLifecyclePort;
  }

  public EidasCallbackView execute(HandleEidasCallbackCommand command) {
    var stateRecord =
        eidasStatePort
            .consume(command.state())
            .orElseThrow(() -> new EidasInvalidStateException("Invalid or expired eIDAS state"));

    if (!stateRecord.providerCode().equals(command.providerCode())) {
      throw new EidasInvalidStateException("eIDAS callback provider does not match the login state");
    }

    if (stateRecord.expiresAt().isBefore(Instant.now())) {
      throw new EidasInvalidStateException("eIDAS login state expired");
    }

    var brokerIdentity =
        eidasBrokerPort.exchangeAuthorizationCode(
            command.providerCode(), command.state(), command.authorizationCode());

    Optional<Long> linkedUserId =
        externalIdentityLifecyclePort.findLinkedUserId(
            command.providerCode(), brokerIdentity.externalSubjectId());

    Instant processedAt = Instant.now();
    boolean profileCompletionRequired = linkedUserId.isEmpty();
    String currentStatus = profileCompletionRequired ? "PENDING_PROFILE" : "ACTIVE";
    externalIdentityLifecyclePort.save(
        new ExternalIdentityView(
            null,
            linkedUserId.orElse(null),
            command.providerCode(),
            "EIDAS_BROKER",
            brokerIdentity.externalSubjectId(),
            brokerIdentity.levelOfAssurance(),
            brokerIdentity.personIdentifier(),
            brokerIdentity.legalPersonIdentifier(),
            brokerIdentity.personIdentifier(),
            null,
            brokerIdentity.firstName(),
            brokerIdentity.familyName(),
            brokerIdentity.dateOfBirth(),
            buildRawClaimsJson(brokerIdentity),
            currentStatus,
            null,
            null,
            null,
            linkedUserId.isPresent() ? processedAt : null,
            null,
            processedAt,
            processedAt));

    return new EidasCallbackView(
        command.providerCode(),
        brokerIdentity.externalSubjectId(),
        brokerIdentity.levelOfAssurance(),
        currentStatus,
        linkedUserId.orElse(null),
      profileCompletionRequired,
        processedAt);
  }

  private static String buildRawClaimsJson(EidasBrokerIdentityView identity) {
    return "{"
        + "\"externalSubjectId\":\""
        + escape(identity.externalSubjectId())
        + "\","
        + "\"levelOfAssurance\":\""
        + escape(identity.levelOfAssurance())
        + "\","
        + "\"personIdentifier\":\""
        + escape(identity.personIdentifier())
        + "\","
        + "\"legalPersonIdentifier\":\""
        + escape(identity.legalPersonIdentifier())
        + "\","
        + "\"firstName\":\""
        + escape(identity.firstName())
        + "\","
        + "\"familyName\":\""
        + escape(identity.familyName())
        + "\","
        + "\"dateOfBirth\":\""
        + escape(identity.dateOfBirth())
        + "\""
        + "}";
  }

  private static String escape(String value) {
    return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
