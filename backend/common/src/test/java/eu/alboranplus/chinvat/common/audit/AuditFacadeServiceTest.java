package eu.alboranplus.chinvat.common.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.then;

import eu.alboranplus.chinvat.common.audit.persistence.AuditEventJpaEntity;
import eu.alboranplus.chinvat.common.audit.persistence.AuditEventJpaRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditFacadeServiceTest {

  @Mock private AuditEventJpaRepository auditEventJpaRepository;

  @InjectMocks private AuditFacadeService sut;

  @Test
  void log_enrichesDetailsWithActorAndPersistsAuditEntry() {
    sut.log(
        "AUTH_LOGIN_SUCCEEDED",
        "alice@example.com",
        UUID.fromString("00000000-0000-0000-0000-00000000002a"),
        AuditDetails.builder().add("clientIp", "127.0.0.1").build());

    ArgumentCaptor<AuditEventJpaEntity> captor = forClass(AuditEventJpaEntity.class);
    then(auditEventJpaRepository).should().save(captor.capture());
    assertThat(captor.getValue().getEventType()).isEqualTo("AUTH_LOGIN_SUCCEEDED");
    assertThat(captor.getValue().getUserId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-00000000002a"));
    assertThat(captor.getValue().getDetails())
        .containsEntry("clientIp", "127.0.0.1")
        .containsEntry("actor", "alice@example.com");
  }
}