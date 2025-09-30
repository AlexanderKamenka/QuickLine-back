package my.app.quickline.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import my.app.quickline.enums.QueueStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "queues")
@Data
public class Queue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;
    
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    
    private String clientName;
    private String clientPhone;
    
    private LocalDateTime appointmentTime;
    
    @Enumerated(EnumType.STRING)
    private QueueStatus status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}