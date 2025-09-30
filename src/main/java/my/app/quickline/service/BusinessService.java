package my.app.quickline.service;

import my.app.quickline.model.entity.Business;
import my.app.quickline.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    
    @Autowired
    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }
    
    public Business createBusiness(Business business) {
        return businessRepository.save(business);
    }
    
    public Business getBusinessById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + id));
    }
    
    public List<Business> getBusinessesByOwner(Long ownerId) {
        return businessRepository.findByOwnerId(ownerId);
    }
    
    public Business updateBusiness(Long id, Business businessDetails) {
        Business business = getBusinessById(id);
        business.setName(businessDetails.getName());
        business.setDescription(businessDetails.getDescription());
        business.setAddress(businessDetails.getAddress());
        business.setPhoneNumber(businessDetails.getPhoneNumber());
        return businessRepository.save(business);
    }
    
    public void deleteBusiness(Long id) {
        Business business = getBusinessById(id);
        businessRepository.delete(business);
    }
}