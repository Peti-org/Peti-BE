package com.peti.backend.model.elastic.model;

import com.peti.backend.model.domain.CaretakerRRule;

public class Capacity {

  private final Integer petCapacity;
  private final Integer peopleCapacity;

  public Capacity(Integer petCapacity, Integer peopleCapacity) {
    this.petCapacity = petCapacity;
    this.peopleCapacity = peopleCapacity;
  }

  public static Capacity fromPositiveRRule(CaretakerRRule rRule) {
    return new Capacity(rRule.getPetCapacity(), rRule.getPeopleCapacity());
  }

  public static Capacity fromNegativeRRule(CaretakerRRule rRule) {
    return new Capacity(-rRule.getPetCapacity(), -rRule.getPeopleCapacity());
  }

  public static Capacity fromPositiveBooking(BookingInput booking) {
    //Event/Booking can be assigned to one pet owner, so peopleCount is always 1.
    return new Capacity(booking.bookedCapacity(), 1);
  }

  public static Capacity fromNegativeBooking(BookingInput booking) {
    //Event/Booking can be assigned to one pet owner, so peopleCount is always 1.
    return new Capacity(-booking.bookedCapacity(), -1);
  }

  public boolean isPositive() {
    return petCapacity > 0 && peopleCapacity > 0;
  }

  public Integer getCapacity() {
    return petCapacity;
  }

  public Capacity sum(Capacity other) {
    return new Capacity(this.petCapacity + other.petCapacity, this.peopleCapacity + other.peopleCapacity);
  }

  public Capacity clampToZero() {
    return new Capacity(Math.max(0, petCapacity), Math.max(0, peopleCapacity));
  }


}
