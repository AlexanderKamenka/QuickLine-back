package my.app.quickline.controller;

import my.app.quickline.enums.QueueStatus;
import my.app.quickline.model.entity.Queue;
import my.app.quickline.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;
    
    @Autowired
    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }
    
    @PostMapping
    public ResponseEntity<Queue> createQueue(@RequestBody Queue queue) {
        return new ResponseEntity<>(queueService.createQueue(queue), HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Queue> getQueueById(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.getQueueById(id));
    }
    
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Queue>> getQueuesByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(queueService.getQueuesByBusiness(businessId));
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Queue>> getQueuesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(queueService.getQueuesByClient(clientId));
    }
    
    @GetMapping("/business/{businessId}/date/{date}")
    public ResponseEntity<List<Queue>> getQueuesByBusinessAndDay(
            @PathVariable Long businessId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(queueService.getQueuesByBusinessAndDay(businessId, date));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Queue> updateQueueStatus(@PathVariable Long id, @RequestParam QueueStatus status) {
        return ResponseEntity.ok(queueService.updateQueueStatus(id, status));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }
}