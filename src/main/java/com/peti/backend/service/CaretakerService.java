package com.peti.backend.service;

import com.peti.backend.dto.CaretakerDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.repository.CaretakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CaretakerService {

    @Autowired
    private CaretakerRepository caretakerRepository;

        private CaretakerDto mapToDto(Caretaker caretaker) {
        CaretakerDto caretakerDto = new CaretakerDto();
//        caretakerDto.setId(caretaker.getId());
//        caretakerDto.setName(caretaker.getName());
//        caretakerDto.setSurname(caretaker.getSurname());
//        caretakerDto.setPhone(caretaker.getPhone());
//        caretakerDto.setMail(caretaker.getMail());
//        caretakerDto.setRating(caretaker.getRating());
        return caretakerDto;
    }

    public List<CaretakerDto> getAllCaretakers() {
        List<Caretaker> caretakers = caretakerRepository.findAll();
        return caretakers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<CaretakerDto> getCaretakerById(Long id) {
        Optional<Caretaker> caretakerOptional = caretakerRepository.findById(id);
        return caretakerOptional.map(this::mapToDto);
    }

    public CaretakerDto createCaretaker(Caretaker caretaker) {
        Caretaker savedCaretaker = caretakerRepository.save(caretaker);
        return mapToDto(savedCaretaker);
    }

    public CaretakerDto updateCaretaker(Long id, Caretaker caretaker) {
        if (caretakerRepository.existsById(id)) {
//            caretaker.setId(id);
            return mapToDto(caretakerRepository.save(caretaker));
        }
        return null;
    }

    public void deleteCaretaker(Long id) {
        caretakerRepository.deleteById(id);
    }
}
