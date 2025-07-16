package com.peti.backend.controller;

import com.peti.backend.service.PaymentSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-settings")
public class PaymentSettingsController {

    @Autowired
    private PaymentSettingsService paymentSettingsService;

//    @PostMapping
//    public ResponseEntity<PaymentSettingsDto> save(@RequestBody PaymentSettingsDto paymentSettingsDto) {
//        PaymentSettingsDto createdPaymentSettings = paymentSettingsService.save(paymentSettingsDto);
//        return new ResponseEntity<>(createdPaymentSettings, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{userId}/{paymentType}")
//    public ResponseEntity<PaymentSettingsDto> findById(@PathVariable Long userId, @PathVariable String paymentType) {
//         PaymentSettingsPK paymentSettingsPK = new PaymentSettingsPK();
//         paymentSettingsPK.setUserId(userId);
//         paymentSettingsPK.setPaymentType(paymentType);
//
//        return paymentSettingsService.findById(paymentSettingsPK)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//
//    }
//
//    @GetMapping
//    public ResponseEntity<List<PaymentSettingsDto>> findAll() {
//        List<PaymentSettingsDto> paymentSettingsList = paymentSettingsService.findAll();
//        return new ResponseEntity<>(paymentSettingsList, HttpStatus.OK);
//    }
//
//     @DeleteMapping("/{userId}/{paymentType}")
//     public ResponseEntity<Void> deleteById(@PathVariable Long userId, @PathVariable String paymentType) {
//        PaymentSettingsPK paymentSettingsPK = new PaymentSettingsPK();
//        paymentSettingsPK.setUserId(userId);
//        paymentSettingsPK.setPaymentType(paymentType);
//
//         if (paymentSettingsService.existsById(paymentSettingsPK)) {
//             paymentSettingsService.deleteById(paymentSettingsPK);
//             return ResponseEntity.noContent().build();
//         } else {
//             return ResponseEntity.notFound().build();
//         }
//    }
//
//    @DeleteMapping
//    public ResponseEntity<Void> delete(@RequestBody PaymentSettingsDto paymentSettingsDto) {
//        paymentSettingsService.delete(paymentSettingsDto);
//        return ResponseEntity.noContent().build();
//    }

}
