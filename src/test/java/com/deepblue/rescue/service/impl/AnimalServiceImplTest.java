package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private AnimalServiceImpl animalService;

    @Test
    void shouldReturnTrueWhenAnimalIsUnderEvaluation() {
        Animal animal = createAnimalWithStatus(RescueStatus.UNDER_EVALUATION);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isTrue();

        verify(animalRepository).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnTrueWhenAnimalIsInRehabilitation() {
        Animal animal = createAnimalWithStatus(RescueStatus.IN_REHABILITATION);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isTrue();

        verify(animalRepository).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnFalseWhenAnimalIsReleased() {
        Animal animal = createAnimalWithStatus(RescueStatus.RELEASED);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isFalse();

        verify(animalRepository).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnFalseWhenAnimalHasNoRescueCase() {
        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setRescueCase(null);

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isFalse();

        verify(animalRepository).findByAnimalCode("AN-001");
    }

    @Test
    void shouldThrowExceptionWhenAnimalDoesNotExist() {
        when(animalRepository.findByAnimalCode("AN-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.canReceiveTreatment("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(animalRepository).findByAnimalCode("AN-999");
    }

    private Animal createAnimalWithStatus(RescueStatus status) {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RC-001");
        rescueCase.setRescueDate(LocalDate.of(2026, 1, 1));
        rescueCase.setStatus(status);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setRescueCase(rescueCase);

        return animal;
    }
}