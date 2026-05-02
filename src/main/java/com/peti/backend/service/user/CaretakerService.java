package com.peti.backend.service.user;

import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.SimpleCaretakerDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.utils.DeepCloner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaretakerService {

  private final CaretakerRepository caretakerRepository;
  private final UserService userService;
  private final RoleService roleService;
  private final DeepCloner deepCloner;

  public List<SimpleCaretakerDto> getAllCaretakers() {
    List<Caretaker> caretakers = caretakerRepository.findAll();
    return caretakers.stream()
        .map(SimpleCaretakerDto::convert)
        .collect(Collectors.toList());
  }

  public Optional<CaretakerDto> getCaretakerById(UUID id) {
    return caretakerRepository.findById(id).map(this::mapToDto);
  }

  public Optional<UUID> getCaretakerIdByUserId(UUID userId) {
    return caretakerRepository.findCaretakerIdBy(userId);
  }

  @Transactional
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

  public CaretakerDto updateCaretaker(UserProjection userProjection, CaretakerPreferences caretakerPreferences) {
    Caretaker exsistingCaretaker = caretakerRepository.findByUserReference_UserId(userProjection.getUserId())
        .orElseThrow(() -> new BadRequestException("Caretaker not exists"));
    //add validation....
    //rating can't be updated here
    exsistingCaretaker.setCaretakerPreference(deepCloner.deepCopyPreference(caretakerPreferences));
    Caretaker savedCaretaker = caretakerRepository.save(exsistingCaretaker);
    return mapToDto(savedCaretaker);
  }

  private CaretakerDto mapToDto(Caretaker caretaker) {
    return CaretakerDto.convert(caretaker, deepCloner.deepCopyPreference(caretaker.getCaretakerPreference()));
  }

  private Caretaker toCareTaker(UserProjection userProjection) {
    Caretaker caretaker = new Caretaker();
    caretaker.setUserReference(new User(userProjection.getUserId()));
    caretaker.setCaretakerIsDeleted(false);
    caretaker.setRating(0);
    caretaker.setCaretakerPreference(new CaretakerPreferences(null, null));

    return caretaker;
  }
}
