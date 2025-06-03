package com.peti.backend.service;

import com.peti.backend.dto.PaymentSettingsDto;
import com.peti.backend.model.Location;
import com.peti.backend.model.PaymentSettings;
import com.peti.backend.model.PaymentSettingsPK;
import com.peti.backend.repository.PaymentSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@Service
public class PaymentSettingsService {

    @Autowired
    private PaymentSettingsRepository paymentSettingsRepository;

    public List<PaymentSettingsDto> findAll() {
        return paymentSettingsRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Optional<PaymentSettingsDto> findById(PaymentSettingsPK id) {
        return paymentSettingsRepository.findById(id).map(this::convertToDto);
    }

    public PaymentSettingsDto save(PaymentSettings paymentSettings) {
        return convertToDto(paymentSettingsRepository.save(paymentSettings));
    }

    public void deleteById(PaymentSettingsPK id) {
        paymentSettingsRepository.deleteById(id);
    }

    public boolean existsById(PaymentSettingsPK id){
        return paymentSettingsRepository.existsById(id);}

    public void delete(PaymentSettings paymentSettings){
        paymentSettingsRepository.delete(paymentSettings);}


    private PaymentSettingsDto convertToDto(PaymentSettings paymentSettings){
        PaymentSettingsDto paymentSettingsDto = new PaymentSettingsDto();

//        paymentSettingsDto.setUserId(paymentSettings.getId().getUserId());
//        paymentSettingsDto.setCaretakerId(paymentSettings.getId().getCaretakerId());
//        paymentSettingsDto.setPaymentMethod(paymentSettings.getPaymentMethod());
//        paymentSettingsDto.setAccountNumber(paymentSettings.getAccountNumber());
//        paymentSettingsDto.setExpiryDate(paymentSettings.getExpiryDate());
//        paymentSettingsDto.setCvv(paymentSettings.getCvv());
//        paymentSettingsDto.setCardholderName(paymentSettings.getCardholderName());

        return paymentSettingsDto;
    }
}