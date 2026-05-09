package eu.alboranplus.chinvat.eidas.application.facade;

import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.command.InitiateEidasLoginCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.command.CompleteEidasProfileCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProfileCompletionView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import java.util.List;

public interface EidasFacade {
  EidasLoginView initiateLogin(InitiateEidasLoginCommand command);

  EidasCallbackView handleCallback(HandleEidasCallbackCommand command);

  EidasProfileCompletionView completeProfile(CompleteEidasProfileCommand command, String actor);

  List<EidasProviderView> listProviders();
}
