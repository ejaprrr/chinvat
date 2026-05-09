package eu.alboranplus.chinvat.eidas.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface EidasStatePort {

  void save(String state, String providerCode, Instant expiresAt);

  Optional<EidasStateRecord> consume(String state);

  Optional<EidasStateRecord> find(String state);

  record EidasStateRecord(String state, String providerCode, Instant expiresAt) {}
}
