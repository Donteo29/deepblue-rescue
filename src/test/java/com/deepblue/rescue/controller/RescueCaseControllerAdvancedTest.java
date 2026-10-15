package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.service.RescueCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RescueCaseController.class)
@Import(GlobalExceptionHandler.class)
class RescueCaseControllerAdvancedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RescueCaseService rescueCaseService;

    @Test
    void shouldChangeCaseStatus() throws Exception {
        when(rescueCaseService.changeStatus(
                eq("RES-001"), any(ChangeRescueStatusRequest.class)))
                .thenReturn(new RescueCaseResponse(
                        1L, "RES-001", LocalDate.of(2026, 8, 20),
                        "Santa Marta", RescueStatus.UNDER_EVALUATION,
                        "DB-CAR", "AN-001"
                ));

        mockMvc.perform(patch("/api/rescue-cases/RES-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNDER_EVALUATION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_EVALUATION"));

        verify(rescueCaseService).changeStatus(
                eq("RES-001"), any(ChangeRescueStatusRequest.class));
    }

    @Test
    void shouldReturnConflictWhenBusinessRuleFails() throws Exception {
        when(rescueCaseService.changeStatus(
                eq("RES-001"), any(ChangeRescueStatusRequest.class)))
                .thenThrow(new BusinessRuleException("Invalid status transition"));

        mockMvc.perform(patch("/api/rescue-cases/RES-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RELEASED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    void shouldReturn500ForUnexpectedException() throws Exception {
        when(rescueCaseService.findByCode("BROKEN"))
                .thenThrow(new IllegalStateException("Internal detail"));

        mockMvc.perform(get("/api/rescue-cases/BROKEN"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
