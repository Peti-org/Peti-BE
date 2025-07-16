package com.peti.backend.dto;

import com.peti.backend.model.domain.CaretakerSlot;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

@Getter
@Setter
public class CaretakerSlotDto {
  private UUID slotId;

  private CaretakerDto caretaker;

  private Date date;

  private Time timeFrom;

  private Time timeTo;

  private String type;

  private BigDecimal price;

  private String currency;

  public CaretakerSlotDto(CaretakerSlot slot) {
    this.slotId = slot.getSlotId();
    this.caretaker = new CaretakerDto(slot.getCaretaker());
    this.date = slot.getDate();
    this.timeFrom = slot.getTimeFrom();
    this.timeTo = slot.getTimeTo();
    this.type = slot.getType();
    this.price = slot.getPrice();
    this.currency = slot.getCurrency();
  }
}
