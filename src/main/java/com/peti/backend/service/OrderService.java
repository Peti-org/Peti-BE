package com.peti.backend.service;

import com.peti.backend.dto.OrderDto;
import com.peti.backend.model.Order;
import com.peti.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {


    @Autowired
    private OrderRepository orderRepository;

    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> orderDtos = new ArrayList<>();
        for (Order order : orders) {
            orderDtos.add(mapOrderToDto(order));
        }
        return orderDtos;
    }

    public Optional<OrderDto> getOrderById(Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        return orderOptional.map(this::mapOrderToDto);
    }

    public OrderDto createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        return mapOrderToDto(savedOrder);
    }

//    public OrderDto updateOrder(Long id, Order order) {
//        Optional<Order> orderOptional = orderRepository.findById(id);
//        if (orderOptional.isPresent()) {
//            Order existingOrder = orderOptional.get();
//            existingOrder.setDate(order.getDate());
//            existingOrder.setPet(order.getPet());
//            existingOrder.setUser(order.getUser());
//            Order updatedOrder = orderRepository.save(existingOrder);
//            return mapOrderToDto(updatedOrder);
//
//        }
//        return null;
//    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    private OrderDto mapOrderToDto(Order order) {
        OrderDto orderDto = new OrderDto();
//        orderDto.setId(order.getId());
//        orderDto.setDate(order.getDate());
//        orderDto.setPet(order.getPet());
//        orderDto.setUser(order.getUser());
        return orderDto;
    }
    }