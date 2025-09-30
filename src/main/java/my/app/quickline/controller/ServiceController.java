package my.app.quickline.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.app.quickline.model.dto.ServiceDto;
import my.app.quickline.service.ServiceManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/services")
@RequiredArgsConstructor
public class ServiceController {
    
    private final ServiceManagementService serviceService;
    
    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllBusinessServices(@PathVariable Long businessId) {
        return ResponseEntity.ok(serviceService.getAllBusinessServices(businessId));
    }
    
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDto> getServiceById(
            @PathVariable Long businessId,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getServiceById(businessId, serviceId));
    }
    
    @PostMapping
    @PreAuthorize("@securityService.isBusinessOwner(#businessId, authentication.principal)")
    public ResponseEntity<ServiceDto> createService(
            @PathVariable Long businessId,
            @Valid @RequestBody ServiceDto serviceDto) {
        return new ResponseEntity<>(serviceService.createService(businessId, serviceDto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{serviceId}")
    @PreAuthorize("@securityService.isBusinessOwner(#businessId, authentication.principal)")
    public ResponseEntity<ServiceDto> updateService(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceDto serviceDto) {
        return ResponseEntity.ok(serviceService.updateService(businessId, serviceId, serviceDto));
    }
    
    @DeleteMapping("/{serviceId}")
    @PreAuthorize("@securityService.isBusinessOwner(#businessId, authentication.principal)")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long businessId,
            @PathVariable Long serviceId) {
        serviceService.deleteService(businessId, serviceId);
        return ResponseEntity.noContent().build();
    }

}