package com.peti.backend.repository;

import com.peti.backend.model.domain.PaymentSettings;
import com.peti.backend.model.domain.PaymentSettingsPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSettingsRepository extends JpaRepository<PaymentSettings, PaymentSettingsPK> {
}
