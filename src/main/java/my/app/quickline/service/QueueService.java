package my.app.quickline.service;

import my.app.quickline.enums.QueueStatus;
import my.app.quickline.model.entity.Queue;
import my.app.quickline.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    
    @Autowired
    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }
    
    public Queue createQueue(Queue queue) {
        return queueRepository.save(queue);
    }
    
    public Queue getQueueById(Long id) {
        return queueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Queue entry not found with id: " + id));
    }
    
    public List<Queue> getQueuesByBusiness(Long businessId) {
        return queueRepository.findByBusinessId(businessId);
    }
    
    public List<Queue> getQueuesByClient(Long clientId) {
        return queueRepository.findByClientId(clientId);
    }
    
    public List<Queue> getQueuesByBusinessAndDay(Long businessId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return queueRepository.findByBusinessIdAndAppointmentTimeBetween(businessId, startOfDay, endOfDay);
    }
    
    public Queue updateQueueStatus(Long id, QueueStatus status) {
        Queue queue = getQueueById(id);
        queue.setStatus(status);
        return queueRepository.save(queue);
    }
    
    public void deleteQueue(Long id) {
        Queue queue = getQueueById(id);
        queueRepository.delete(queue);
    }
}