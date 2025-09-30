package my.app.quickline.service;

import lombok.RequiredArgsConstructor;
import my.app.quickline.exception.BadRequestException;
import my.app.quickline.exception.ResourceNotFoundException;
import my.app.quickline.model.dto.ServiceDto;
import my.app.quickline.model.entity.Business;
import my.app.quickline.model.entity.Service;
import my.app.quickline.repository.BusinessRepository;
import my.app.quickline.repository.ServiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServiceManagementService {
    
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    
    // Получение всех услуг бизнеса
    public List<ServiceDto> getAllBusinessServices(Long businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Бизнес с ID " + businessId + " не найден");
        }
        
        return serviceRepository.findByBusinessId(businessId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    // Получение услуги по ID
    public ServiceDto getServiceById(Long businessId, Long serviceId) {
        return serviceRepository.findByIdAndBusinessId(serviceId, businessId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + serviceId + 
                        " для бизнеса с ID " + businessId + " не найдена"));
    }
    
    // Создание новой услуги
    @Transactional
    public ServiceDto createService(Long businessId, ServiceDto serviceDto) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Бизнес с ID " + businessId + " не найден"));
        
        Service service = Service.builder()
                .name(serviceDto.getName())
                .description(serviceDto.getDescription())
                .price(serviceDto.getPrice())
                .durationMinutes(serviceDto.getDurationMinutes())
                .business(business)
                .build();
        
        Service savedService = serviceRepository.save(service);
        return mapToDto(savedService);
    }
    
    // Обновление услуги
    @Transactional
    public ServiceDto updateService(Long businessId, Long serviceId, ServiceDto serviceDto) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Бизнес с ID " + businessId + " не найден");
        }
        
        Service service = serviceRepository.findByIdAndBusinessId(serviceId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + serviceId + 
                        " для бизнеса с ID " + businessId + " не найдена"));
        
        service.setName(serviceDto.getName());
        service.setDescription(serviceDto.getDescription());
        service.setPrice(serviceDto.getPrice());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        
        Service updatedService = serviceRepository.save(service);
        return mapToDto(updatedService);
    }
    
    // Удаление услуги
    @Transactional
    public void deleteService(Long businessId, Long serviceId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Бизнес с ID " + businessId + " не найден");
        }
        
        Service service = serviceRepository.findByIdAndBusinessId(serviceId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + serviceId + 
                        " для бизнеса с ID " + businessId + " не найдена"));
        
        // Здесь можно добавить проверку на наличие активных записей на эту услугу
        // и выбросить исключение, если такие записи есть
        
        serviceRepository.delete(service);
    }
    
    // Вспомогательный метод для преобразования сущности в DTO
    private ServiceDto mapToDto(Service service) {
        return ServiceDto.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .businessId(service.getBusiness().getId())
                .build();
    }
}