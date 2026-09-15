package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository rescueCaseRepository;

    @Mock
    private RescueCaseMapper rescueCaseMapper;

    @InjectMocks
    private RescueCaseServiceImpl rescueCaseService;

    private RescueCase rescueCase;
    private RescueCaseResponse rescueCaseResponse;

    @BeforeEach
    void setUp() {
        rescueCase = new RescueCase();
        rescueCase.setCaseCode("RC-001");
        rescueCase.setStatus(RescueStatus.ADMITTED);

        rescueCaseResponse = new RescueCaseResponse(
                1L,
                "RC-001",
                null,
                null,
                RescueStatus.ADMITTED,
                null,
                null
        );
    }

    @Test
    void shouldFindRescueCaseByCode() {
        when(rescueCaseRepository.findByCaseCode("RC-001"))
                .thenReturn(Optional.of(rescueCase));

        when(rescueCaseMapper.toResponse(rescueCase))
                .thenReturn(rescueCaseResponse);

        RescueCaseResponse result =
                rescueCaseService.findByCode("RC-001");

        assertThat(result).isEqualTo(rescueCaseResponse);

        verify(rescueCaseRepository).findByCaseCode("RC-001");
        verify(rescueCaseMapper).toResponse(rescueCase);
    }

    @Test
    void shouldThrowExceptionWhenRescueCaseDoesNotExist() {
        when(rescueCaseRepository.findByCaseCode("RC-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                rescueCaseService.findByCode("RC-999")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rescue case not found: RC-999");

        verify(rescueCaseRepository).findByCaseCode("RC-999");
        verifyNoInteractions(rescueCaseMapper);
    }

    @Test
    void shouldFindRescueCasesByStatus() {
        when(rescueCaseRepository.findByStatus(RescueStatus.ADMITTED))
                .thenReturn(List.of(rescueCase));

        when(rescueCaseMapper.toResponse(rescueCase))
                .thenReturn(rescueCaseResponse);

        List<RescueCaseResponse> result =
                rescueCaseService.findByStatus(RescueStatus.ADMITTED);

        assertThat(result)
                .hasSize(1)
                .containsExactly(rescueCaseResponse);

        verify(rescueCaseRepository)
                .findByStatus(RescueStatus.ADMITTED);

        verify(rescueCaseMapper).toResponse(rescueCase);
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        ChangeRescueStatusRequest request =
                new ChangeRescueStatusRequest(
                        RescueStatus.UNDER_EVALUATION
                );

        RescueCaseResponse updatedResponse =
                new RescueCaseResponse(
                        1L,
                        "RC-001",
                        null,
                        null,
                        RescueStatus.UNDER_EVALUATION,
                        null,
                        null
                );

        when(rescueCaseRepository.findByCaseCode("RC-001"))
                .thenReturn(Optional.of(rescueCase));

        when(rescueCaseRepository.save(rescueCase))
                .thenReturn(rescueCase);

        when(rescueCaseMapper.toResponse(rescueCase))
                .thenReturn(updatedResponse);

        RescueCaseResponse result =
                rescueCaseService.changeStatus("RC-001", request);

        assertThat(result).isEqualTo(updatedResponse);
        assertThat(rescueCase.getStatus())
                .isEqualTo(RescueStatus.UNDER_EVALUATION);

        verify(rescueCaseRepository).save(rescueCase);
        verify(rescueCaseMapper).toResponse(rescueCase);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        ChangeRescueStatusRequest request =
                new ChangeRescueStatusRequest(
                        RescueStatus.RELEASED
                );

        when(rescueCaseRepository.findByCaseCode("RC-001"))
                .thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() ->
                rescueCaseService.changeStatus("RC-001", request)
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "Invalid status transition from ADMITTED to RELEASED"
                );

        verify(rescueCaseRepository, never()).save(any());
        verifyNoInteractions(rescueCaseMapper);
    }
}