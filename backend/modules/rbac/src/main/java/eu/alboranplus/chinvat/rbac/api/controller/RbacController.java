package eu.alboranplus.chinvat.rbac.api.controller;

import eu.alboranplus.chinvat.rbac.api.dto.RoleResponse;
import eu.alboranplus.chinvat.rbac.api.mapper.RbacApiMapper;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rbac")
public class RbacController {

  private final RbacFacade rbacFacade;
  private final RbacApiMapper rbacApiMapper;

  public RbacController(RbacFacade rbacFacade, RbacApiMapper rbacApiMapper) {
    this.rbacFacade = rbacFacade;
    this.rbacApiMapper = rbacApiMapper;
  }

  @GetMapping("/roles/{roleName}")
  public ResponseEntity<RoleResponse> getRole(@PathVariable String roleName) {
    RoleView roleView = rbacFacade.getRole(roleName);
    return ResponseEntity.ok(rbacApiMapper.toResponse(roleView));
  }
}
