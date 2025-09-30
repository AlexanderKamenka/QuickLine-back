package my.app.quickline.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    private Long id;
    
    @NotBlank(message = "Название услуги не может быть пустым")
    private String name;
    
    private String description;
    
    @NotNull(message = "Цена услуги должна быть указана")
    @Positive(message = "Цена услуги должна быть положительной")
    private BigDecimal price;
    
    @NotNull(message = "Длительность услуги должна быть указана")
    @Positive(message = "Длительность услуги должна быть положительной")
    private Integer durationMinutes;
    
    private Long businessId;
}