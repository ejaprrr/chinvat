package eu.alboranplus.chinvat.auth.application.port.out;

import java.util.Set;

public interface AuthRbacPort {
  Set<String> resolvePermissions(Set<String> roleNames);
}
