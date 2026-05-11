package eu.alboranplus.chinvat.eidas.api.controller;

import eu.alboranplus.chinvat.eidas.api.dto.EidasCallbackRequest;
import eu.alboranplus.chinvat.eidas.api.dto.EidasCallbackResponse;
import eu.alboranplus.chinvat.eidas.api.dto.EidasLoginRequest;
import eu.alboranplus.chinvat.eidas.api.dto.EidasLoginResponse;
import eu.alboranplus.chinvat.eidas.api.dto.EidasProviderResponse;
import eu.alboranplus.chinvat.eidas.api.mapper.EidasApiMapper;
import eu.alboranplus.chinvat.eidas.application.facade.EidasFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "eIDAS", description = "eIDAS login initiation, callback handling and provider registry")
@RestController
@RequestMapping("/api/v1/auth/eidas")
public class EidasController {

  private final EidasFacade eidasFacade;
  private final EidasApiMapper eidasApiMapper;

  public EidasController(EidasFacade eidasFacade, EidasApiMapper eidasApiMapper) {
    this.eidasFacade = eidasFacade;
    this.eidasApiMapper = eidasApiMapper;
  }

  @Operation(summary = "Initiate eIDAS login", description = "Starts the eIDAS flow and returns authorization URL and state.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "eIDAS login initiated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EidasLoginResponse.class))),
    @ApiResponse(responseCode = "404", description = "Provider not found or disabled")
  })
  @PostMapping("/login")
  public ResponseEntity<EidasLoginResponse> initiateLogin(@Valid @RequestBody EidasLoginRequest request) {
    var result = eidasFacade.initiateLogin(eidasApiMapper.toCommand(request));
    return ResponseEntity.ok(eidasApiMapper.toResponse(result));
  }

  @Operation(summary = "Handle eIDAS callback", description = "Validates callback state and returns identity linking decision data.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "eIDAS callback processed",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EidasCallbackResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid callback state")
  })
  @PostMapping("/callback")
  public ResponseEntity<EidasCallbackResponse> handleCallback(
      @Valid @RequestBody EidasCallbackRequest request) {
    var result = eidasFacade.handleCallback(eidasApiMapper.toCommand(request));
    return ResponseEntity.ok(eidasApiMapper.toResponse(result));
  }

  @Operation(summary = "List eIDAS providers", description = "Returns configured eIDAS providers with enabled/disabled status.")
  @GetMapping("/providers")
  public ResponseEntity<List<EidasProviderResponse>> listProviders() {
    var providers = eidasFacade.listProviders().stream().map(eidasApiMapper::toResponse).toList();
    return ResponseEntity.ok(providers);
  }
  
    @Operation(summary = "List eIDAS providers with pagination", description = "Returns configured eIDAS providers with pagination support.")
    @GetMapping("/providers/paged")
    public ResponseEntity<PageResponse<EidasProviderResponse>> listProviders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort) {
      PaginationRequest paginationRequest = new PaginationRequest(page, size, sort);
      PageResponse<EidasProviderView> pageResponse = eidasFacade.listProvidersPaged(paginationRequest);
    
      List<EidasProviderResponse> responseData = pageResponse.data().stream()
          .map(eidasApiMapper::toResponse).toList();
    
      return ResponseEntity.ok(PageResponse.of(responseData, pageResponse.pagination()));
    }
}
