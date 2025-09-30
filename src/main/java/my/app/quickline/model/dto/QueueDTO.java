package my.app.quickline.model.dto;

import lombok.Data;

@Data
public class QueueDTO {
    private Long id;
    private Long businessId;
    private Long serviceId;
    private String serviceName;
    private String clientName;
    private String clientPhone;
    private String appointmentTime;
    private String status;
}
