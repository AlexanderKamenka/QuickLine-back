package my.app.quickline.repository;

import my.app.quickline.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByBusinessId(Long businessId);
    
    Optional<Service> findByIdAndBusinessId(Long id, Long businessId);
    
    // Если вы добавите поле active
    List<Service> findByBusinessIdAndActiveTrue(Long businessId);
}