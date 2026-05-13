package eu.alboranplus.chinvat.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "eu.alboranplus.chinvat",
    importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

  @ArchTest
  static final ArchRule api_should_not_depend_on_infrastructure =
      classes()
          .that()
          .resideInAPackage("..api..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "..api..",
              "..application..",
              "..domain..",
              "..contracts..",
              "..common.pagination..",
              "java..",
              "jakarta..",
              "org.springframework..",
              "com.fasterxml.jackson..",
              "io.swagger.v3.oas.annotations..")
          .because("API should depend on use cases and API-facing models, not infrastructure.");

  @ArchTest
  static final ArchRule application_should_not_depend_on_api_or_infrastructure =
      classes()
          .that()
          .resideInAPackage("..application..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "..application..",
              "..domain..",
              "..contracts..",
              "..common..",
              "java..",
              "jakarta..",
              "org.springframework..")
          .because("Application code should stay independent from API and infrastructure details. "
              + "Common module exceptions: AuditFacade and PermissionCacheFacade are cross-cutting concerns needed by all modules.");

  @ArchTest
  static final ArchRule contracts_should_not_depend_on_spring_or_other_layers =
      classes()
          .that()
          .resideInAPackage("..contracts..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("..contracts..", "java..")
          .because("Contracts must stay framework-agnostic and stable.")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domain_should_not_depend_on_spring_or_other_layers =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("..domain..", "java..")
          .because("Domain code should remain framework-agnostic.");

  @ArchTest
  static final ArchRule non_infrastructure_code_should_not_access_infrastructure =
      noClasses()
          .that()
          .resideInAnyPackage("..api..", "..application..", "..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..");

  @ArchTest
  static final ArchRule common_should_not_depend_on_modules =
      noClasses()
          .that()
          .resideInAPackage("..common..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..auth..",
              "..rbac..",
              "..users..",
              "..trust..",
              "..eidas..")
          .because("Common module should be independent of feature modules to prevent cyclic dependencies.");
}
