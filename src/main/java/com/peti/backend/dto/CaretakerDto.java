package com.peti.backend.dto;

import com.peti.backend.model.Caretaker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CaretakerDto {
    private String id;
    private String name;
    private String email;
    private String address;

    public CaretakerDto(Caretaker caretaker) {
        this.id = caretaker.getCaretakerId().toString();
        this.name = caretaker.getUserByUserId().getFirstName();
        this.email = caretaker.getUserByUserId().getEmail();
        this.email = caretaker.getUserByUserId().getLocationByLocationId().getCity();
    }
}