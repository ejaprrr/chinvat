package eu.alboranplus.chinvat.health.application;

import eu.alboranplus.chinvat.health.domain.SystemHealth;

public interface SystemHealthPort {
  SystemHealth check();
}
