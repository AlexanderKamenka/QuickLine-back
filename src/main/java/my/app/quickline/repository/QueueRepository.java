package my.app.quickline.repository;

import my.app.quickline.model.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByBusinessId(Long businessId);
    List<Queue> findByClientId(Long clientId);
    List<Queue> findByBusinessIdAndAppointmentTimeBetween(
            Long businessId, LocalDateTime start, LocalDateTime end);
}