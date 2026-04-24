package eu.alboranplus.chinvat.health.application;

import eu.alboranplus.chinvat.health.domain.SystemHealth;

/**
 * Output port — contract for checking system health.
 * Application layer depends on this interface; infrastructure implements it.
 *
 * Nest analogie: provider interface injected via token / abstract provider.
 */
public interface SystemHealthPort {
    SystemHealth check();
}

