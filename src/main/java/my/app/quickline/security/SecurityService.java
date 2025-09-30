package my.app.quickline.security;

import lombok.RequiredArgsConstructor;
import my.app.quickline.model.entity.Business;
import my.app.quickline.repository.BusinessRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final BusinessRepository businessRepository;
    
    public boolean isBusinessOwner(Long businessId, UserDetails userDetails) {
        return businessRepository.findById(businessId)
                .map(business -> business.getOwner().getUsername().equals(userDetails.getUsername()))
                .orElse(false);
    }
}