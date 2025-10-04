package my.app.quickline.controller;

import my.app.quickline.model.entity.Business;
import my.app.quickline.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;
    
    @Autowired
    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }
    
    @PostMapping
    public ResponseEntity<Business> createBusiness(@RequestBody Business business) {
        return new ResponseEntity<>(businessService.createBusiness(business), HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }
    
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Business>> getBusinessesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(businessService.getBusinessesByOwner(ownerId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business business) {
        return ResponseEntity.ok(businessService.updateBusiness(id, business));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
    // НОВЫЙ ЭНДПОИНТ - получить все бизнесы (для публичной страницы)
    @GetMapping
    public ResponseEntity<List<Business>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }
}