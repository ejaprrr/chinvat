package eu.alboranplus.chinvat.rbac.api.mapper;

import eu.alboranplus.chinvat.rbac.api.dto.RoleResponse;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import org.springframework.stereotype.Component;

@Component
public class RbacApiMapper {

  public RoleResponse toResponse(RoleView roleView) {
    return new RoleResponse(roleView.roleName(), roleView.permissions());
  }
}
