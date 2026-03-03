package com.peti.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.SimpleCaretakerDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.repository.CaretakerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaretakerService {

  private final CaretakerRepository caretakerRepository;
  private final UserService userService;
  private final RoleService roleService;
  private final ObjectMapper objectMapper;

  public static SimpleCaretakerDto convertToSimpleDto(Caretaker caretaker) {
    return new SimpleCaretakerDto(
        caretaker.getCaretakerId(),
        caretaker.getUserReference().getFirstName(),
        caretaker.getUserReference().getLastName(),
        caretaker.getRating()
    );
  }

  private CaretakerDto mapToDto(Caretaker caretaker) {
    CaretakerDto caretakerDto = new CaretakerDto();
    caretakerDto.setId(caretaker.getCaretakerId());
    caretakerDto.setName(caretaker.getUserReference().getFirstName());
    caretakerDto.setEmail(caretaker.getUserReference().getEmail());
    caretakerDto.setRating(caretaker.getRating());
    caretakerDto.setCaretakerPreference(deepCopyPreference(caretaker.getCaretakerPreference()));
    return caretakerDto;
  }

  private Caretaker toCareTaker(UserProjection userProjection) {
    Caretaker caretaker = new Caretaker();
    caretaker.setUserReference(new User(userProjection.getUserId()));
    caretaker.setCaretakerIsDeleted(false);
    caretaker.setRating(0);
    caretaker.setCaretakerPreference(
        new CaretakerPreferences(null, null)); // Assuming a default empty JSON object for preferences

//        caretakerDto.setId(caretaker.getId());
//        caretakerDto.setName(caretaker.getName());
//        caretakerDto.setSurname(caretaker.getSurname());
//        caretakerDto.setPhone(caretaker.getPhone());
//        caretakerDto.setMail(caretaker.getMail());
//        caretakerDto.setRating(caretaker.getRating());
    return caretaker;
  }

  public List<CaretakerDto> getAllCaretakers() {
    List<Caretaker> caretakers = caretakerRepository.findAll();
    return caretakers.stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  public Optional<CaretakerDto> getCaretakerById(UUID id) {
    return caretakerRepository.findById(id).map(this::mapToDto);
  }

  public Optional<UUID> getCaretakerIdByUserId(UUID userId) {
    return caretakerRepository.findCaretakerIdBy(userId);
  }

  public CaretakerDto createCaretaker(UserProjection userProjection) {
    if (caretakerRepository.existsByUserReference_UserId(userProjection.getUserId())) {
      throw new BadRequestException("Caretaker already exists");
    }
    Caretaker savedCaretaker = caretakerRepository.save(toCareTaker(userProjection));
    if (userProjection.getRoleId() > roleService.getCareTakerRole().getRoleId()) {
      // Change role to caretaker if the user has lower role than caretaker, otherwise keep the same role (e.g. admin)
      userService.changeRole(userProjection.getUserId(), roleService.getCareTakerRole());
    }
    return mapToDto(savedCaretaker);
  }

  public CaretakerPreferences deepCopyPreference(CaretakerPreferences preferences) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(preferences);
      return objectMapper.readValue(bytes, new TypeReference<>() {
      });
    } catch (Exception e) {
      throw new RuntimeException("Deep copy failed", e);
    }
  }

  public CaretakerDto updateCaretaker(UserProjection userProjection, CaretakerPreferences caretakerPreferences) {
    Caretaker exsistingCaretaker = caretakerRepository.findByUserReference_UserId(userProjection.getUserId())
        .orElseThrow(() -> new BadRequestException("Caretaker not exists"));
    //add validation....
    //rating can't be updated here
    exsistingCaretaker.setCaretakerPreference(deepCopyPreference(caretakerPreferences));
    Caretaker savedCaretaker = caretakerRepository.save(exsistingCaretaker);
    return mapToDto(savedCaretaker);
  }

//  public void deleteCaretaker(Long id) {
//    caretakerRepository.deleteById(id);
//  }
}
