package eu.alboranplus.chinvat.eidas.infrastructure.adapter;

import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEidasStateAdapter implements EidasStatePort {

  private final Map<String, EidasStateRecord> stateStore = new ConcurrentHashMap<>();

  @Override
  public void save(String state, String providerCode, Instant expiresAt) {
    stateStore.put(state, new EidasStateRecord(state, providerCode, expiresAt));
  }

  @Override
  public Optional<EidasStateRecord> consume(String state) {
    EidasStateRecord removed = stateStore.remove(state);
    return Optional.ofNullable(removed);
  }

  @Override
  public Optional<EidasStateRecord> find(String state) {
    return Optional.ofNullable(stateStore.get(state));
  }
}
