package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.TreatmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TreatmentServiceImpl implements TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final AnimalRepository animalRepository;
    private final SpecialistRepository specialistRepository;
    private final TreatmentMapper treatmentMapper;

    public TreatmentServiceImpl(
            TreatmentRepository treatmentRepository,
            AnimalRepository animalRepository,
            SpecialistRepository specialistRepository,
            TreatmentMapper treatmentMapper
    ) {
        this.treatmentRepository = treatmentRepository;
        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.treatmentMapper = treatmentMapper;
    }

    @Override
    @Transactional
    public TreatmentResponse register(CreateTreatmentRequest request) {

        Animal animal = animalRepository.findByAnimalCode(request.animalCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Animal not found: " + request.animalCode()
                        )
                );

        Specialist specialist =
                specialistRepository.findByProfessionalCode(
                        request.specialistCode()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Specialist not found: "
                                        + request.specialistCode()
                        )
                );

        if (!specialist.isActive()) {
            throw new BusinessRuleException(
                    "The specialist is not active"
            );
        }

        if (animal.getRescueCase() == null) {
            throw new BusinessRuleException(
                    "The animal is not associated with a rescue case"
            );
        }

        RescueStatus animalStatus = animal.getRescueCase().getStatus();

        if (animalStatus == RescueStatus.RELEASED
                || animalStatus == RescueStatus.CLOSED) {
            throw new BusinessRuleException(
                    "Treatments cannot be registered for a released "
                            + "or closed animal"
            );
        }

        if (request.performedAt()
                .toLocalDate()
                .isBefore(animal.getRescueCase().getRescueDate())) {
            throw new BusinessRuleException(
                    "Treatment date cannot be before the rescue date"
            );
        }

        Treatment treatment = new Treatment();

        treatment.setAnimal(animal);
        treatment.setSpecialist(specialist);
        treatment.setPerformedAt(request.performedAt());
        treatment.setType(request.type());
        treatment.setDescription(request.description());

        Treatment savedTreatment = treatmentRepository.save(treatment);

        return treatmentMapper.toResponse(savedTreatment);
    }

    @Override
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        return treatmentRepository
                .findByAnimalAnimalCodeOrderByPerformedAtAsc(animalCode)
                .stream()
                .map(treatmentMapper::toResponse)
                .toList();
    }
}