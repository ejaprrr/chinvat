package eu.alboranplus.chinvat.eidas.application.usecase;

import eu.alboranplus.chinvat.eidas.application.command.InitiateEidasLoginCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasStateTtlPort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProviderNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InitiateEidasLoginUseCase {

  private final EidasBrokerPort eidasBrokerPort;
  private final EidasStatePort eidasStatePort;
  private final EidasStateTtlPort eidasStateTtlPort;

  public InitiateEidasLoginUseCase(
      EidasBrokerPort eidasBrokerPort,
      EidasStatePort eidasStatePort,
      EidasStateTtlPort eidasStateTtlPort) {
    this.eidasBrokerPort = eidasBrokerPort;
    this.eidasStatePort = eidasStatePort;
    this.eidasStateTtlPort = eidasStateTtlPort;
  }

  public EidasLoginView execute(InitiateEidasLoginCommand command) {
    var provider =
        eidasBrokerPort
            .findEnabledProvider(command.providerCode())
            .orElseThrow(
                () ->
                    new EidasProviderNotFoundException(
                        "Enabled eIDAS provider not found: " + command.providerCode()));

    Instant expiresAt = Instant.now().plus(eidasStateTtlPort.stateTtl());
    String state = UUID.randomUUID().toString();
    eidasStatePort.save(state, provider.code(), expiresAt);

    return eidasBrokerPort.initiateLogin(provider.code(), command.redirectUri(), state, expiresAt);
  }
}
