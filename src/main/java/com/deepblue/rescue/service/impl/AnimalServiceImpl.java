package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.AnimalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalServiceImpl(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public boolean canReceiveTreatment(String animalCode) {
        Animal animal = animalRepository
                .findByAnimalCode(animalCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + animalCode
                ));

        if (animal.getRescueCase() == null) {
            return false;
        }

        RescueStatus status = animal.getRescueCase().getStatus();

        return status == RescueStatus.UNDER_EVALUATION
                || status == RescueStatus.IN_REHABILITATION;
    }
}