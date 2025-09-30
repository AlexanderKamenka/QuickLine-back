package my.app.quickline.model.dto;

import lombok.Data;

@Data
public class BusinessDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
}

