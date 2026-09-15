package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentMapper treatmentMapper;

    @InjectMocks
    private TreatmentServiceImpl treatmentService;

    private Animal animal;
    private RescueCase rescueCase;
    private Specialist specialist;
    private CreateTreatmentRequest request;
    private TreatmentResponse treatmentResponse;

    @BeforeEach
    void setUp() {
        rescueCase = new RescueCase();
        rescueCase.setCaseCode("RC-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 9, 1));
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);

        animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setRescueCase(rescueCase);

        specialist = new Specialist();
        specialist.setProfessionalCode("SP-001");
        specialist.setActive(true);

        request = new CreateTreatmentRequest(
                "AN-001",
                "SP-001",
                LocalDateTime.of(2026, 9, 10, 10, 30),
                TreatmentType.MEDICATION,
                "Treatment description"
        );

        treatmentResponse = new TreatmentResponse(
                1L,
                "AN-001",
                "SP-001",
                request.performedAt(),
                request.type(),
                request.description()
        );
    }

    @Test
    void shouldRegisterTreatmentSuccessfully() {
        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SP-001"))
                .thenReturn(Optional.of(specialist));

        Treatment savedTreatment = new Treatment();

        when(treatmentRepository.save(any(Treatment.class)))
                .thenReturn(savedTreatment);

        when(treatmentMapper.toResponse(savedTreatment))
                .thenReturn(treatmentResponse);

        TreatmentResponse result = treatmentService.register(request);

        assertThat(result).isEqualTo(treatmentResponse);

        verify(treatmentRepository).save(any(Treatment.class));
        verify(treatmentMapper).toResponse(savedTreatment);
    }

    @Test
    void shouldRejectTreatmentWhenSpecialistIsInactive() {
        specialist.setActive(false);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SP-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() ->
                treatmentService.register(request)
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The specialist is not active");

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectTreatmentForReleasedAnimal() {
        rescueCase.setStatus(RescueStatus.RELEASED);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SP-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() ->
                treatmentService.register(request)
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Treatments cannot be registered for a released "
                                + "or closed animal"
                );

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectTreatmentBeforeRescueDate() {
        request = new CreateTreatmentRequest(
                "AN-001",
                "SP-001",
                LocalDateTime.of(2026, 8, 30, 10, 30),
                TreatmentType.MEDICATION,
                "Treatment description"
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        when(specialistRepository.findByProfessionalCode("SP-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() ->
                treatmentService.register(request)
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Treatment date cannot be before the rescue date");

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldFindTreatmentsByAnimalCode() {
        Treatment treatment = new Treatment();

        when(treatmentRepository
                .findByAnimalAnimalCodeOrderByPerformedAtAsc("AN-001"))
                .thenReturn(List.of(treatment));

        when(treatmentMapper.toResponse(treatment))
                .thenReturn(treatmentResponse);

        List<TreatmentResponse> result =
                treatmentService.findByAnimalCode("AN-001");

        assertThat(result)
                .hasSize(1)
                .containsExactly(treatmentResponse);

        verify(treatmentRepository)
                .findByAnimalAnimalCodeOrderByPerformedAtAsc("AN-001");

        verify(treatmentMapper).toResponse(treatment);
    }
}