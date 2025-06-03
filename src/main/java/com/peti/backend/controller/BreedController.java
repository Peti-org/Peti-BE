package com.peti.backend.controller;

import com.peti.backend.dto.BreedDto;
import com.peti.backend.model.Breed;
import com.peti.backend.service.BreedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/breeds")
public class BreedController {

    @Autowired
    private BreedService breedService;

    @GetMapping
    public ResponseEntity<List<BreedDto>> getAllBreeds() {
        return new ResponseEntity<>(breedService.getAllBreeds(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BreedDto> getBreedById(@PathVariable Long id) {
        Optional<BreedDto> breedDto = breedService.getBreedById(id);
        return breedDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<BreedDto> createBreed(@RequestBody BreedDto breedDto) {
        return new ResponseEntity<>(breedService.createBreed(breedDto), HttpStatus.CREATED);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<BreedDto> updateBreed(@PathVariable Long id, @RequestBody BreedDto breedDto) {
//        if (!breedService.getBreedById(id).isPresent()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(breedService.updateBreed(id, breedDto), HttpStatus.OK);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBreed(@PathVariable Long id) {
        if (!breedService.getBreedById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        breedService.deleteBreed(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}