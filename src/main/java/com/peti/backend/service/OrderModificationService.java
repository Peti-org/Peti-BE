package com.peti.backend.service;

import com.peti.backend.dto.OrderModificationDto;
import com.peti.backend.model.OrderModification;
import com.peti.backend.repository.OrderModificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderModificationService {

    @Autowired
    private OrderModificationRepository orderModificationRepository;
    
    private OrderModificationDto mapToDto(OrderModification orderModification){
        OrderModificationDto orderModificationDto = new OrderModificationDto();
        orderModificationDto.setModificationDate(orderModification.getModificationDate());
        orderModificationDto.setOrderId(orderModification.getId().getOrderId());
        orderModificationDto.setModificationType(orderModification.getModificationType());
        return orderModificationDto;
    }
    private OrderModification mapToEntity(OrderModificationDto orderModificationDto){
        OrderModification orderModification = new OrderModification();
        orderModification.setModificationDate(orderModificationDto.getModificationDate());
        orderModification.setModificationType(orderModificationDto.getModificationType());
        return orderModification;
    }

    public List<OrderModificationDto> getAllOrderModifications() {
        List<OrderModification> orderModifications = orderModificationRepository.findAll();
        return orderModifications.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Optional<OrderModificationDto> getOrderModificationById(Long id) {
        Optional<OrderModification> orderModification = orderModificationRepository.findById(id);
        if (orderModification.isPresent()){
            return Optional.of(mapToDto(orderModification.get()));
        }else {
            return Optional.empty();
        }
    }

    public OrderModificationDto saveOrderModification(OrderModificationDto orderModificationDto) {
        OrderModification orderModification = mapToEntity(orderModificationDto);
         OrderModification savedOrderModification = orderModificationRepository.save(orderModification);
        return mapToDto(savedOrderModification);
    }

    public void deleteOrderModification(Long id) {
        orderModificationRepository.deleteById(id);
    }

    public OrderModificationDto updateOrderModification(Long id, OrderModificationDto updatedOrderModificationDto) {
        Optional<OrderModification> existingOrderModification = orderModificationRepository.findById(id);
        if (existingOrderModification.isPresent()) {
            OrderModification updatedOrderModification = mapToEntity(updatedOrderModificationDto);
            OrderModification orderModification = existingOrderModification.get();
            orderModification.setModificationDate(updatedOrderModification.getModificationDate());
            orderModification.setModificationType(updatedOrderModification.getModificationType());
            orderModification.setId(updatedOrderModification.getId());

            return mapToDto(orderModificationRepository.save(orderModification));
        }
        return null;
    }
}