package com.peti.backend.controller;

import com.peti.backend.dto.OrderModificationDto;
import com.peti.backend.service.OrderModificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-modifications")
public class OrderModificationController {

    @Autowired
    private OrderModificationService orderModificationService;

    @GetMapping
    public ResponseEntity<List<OrderModificationDto>> getAllOrderModifications() {
        return ResponseEntity.ok(orderModificationService.getAllOrderModifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderModificationDto> getOrderModificationById(@PathVariable Long id) {
        return orderModificationService.getOrderModificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderModificationDto> saveOrderModification(@RequestBody OrderModificationDto orderModificationDto) {
        return new ResponseEntity<>(orderModificationService.saveOrderModification(orderModificationDto), HttpStatus.CREATED);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<OrderModificationDto> updateOrderModification(@PathVariable Long id, @RequestBody OrderModificationDto orderModificationDto){
//         return Optional.ofNullable(orderModificationService.updateOrderModification(id, orderModificationDto))
//               .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteOrderModification(@PathVariable Long id) {
//        if (orderModificationService.deleteOrderModification(id)) {
//            return ResponseEntity.noContent().build();
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
}