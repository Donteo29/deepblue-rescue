package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.RescueCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RescueCaseController.class)
@Import(GlobalExceptionHandler.class)
class RescueCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RescueCaseService rescueCaseService;

    @Test
    void shouldReturnRescueCaseByCode() throws Exception {
        when(rescueCaseService.findByCode("RES-001"))
                .thenReturn(sampleCase());

        mockMvc.perform(get("/api/rescue-cases/RES-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCode").value("RES-001"))
                .andExpect(jsonPath("$.status").value("ADMITTED"));

        verify(rescueCaseService).findByCode("RES-001");
    }

    @Test
    void shouldReturnNotFoundWhenCaseDoesNotExist() throws Exception {
        when(rescueCaseService.findByCode("MISSING"))
                .thenThrow(new ResourceNotFoundException("Rescue case not found: MISSING"));

        mockMvc.perform(get("/api/rescue-cases/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/rescue-cases/MISSING"));
    }

    @Test
    void shouldReturnCasesByStatus() throws Exception {
        when(rescueCaseService.findByStatus(RescueStatus.IN_REHABILITATION))
                .thenReturn(List.of(sampleCase()));

        mockMvc.perform(get("/api/rescue-cases")
                        .param("status", "IN_REHABILITATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].caseCode").value("RES-001"));

        verify(rescueCaseService).findByStatus(RescueStatus.IN_REHABILITATION);
    }

    @Test
    void shouldReturnBadRequestForInvalidStatusQuery() throws Exception {
        mockMvc.perform(get("/api/rescue-cases").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(rescueCaseService);
    }

    @Test
    void shouldValidateChangeStatusRequest() throws Exception {
        mockMvc.perform(patch("/api/rescue-cases/RES-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(rescueCaseService);
    }

    private RescueCaseResponse sampleCase() {
        return new RescueCaseResponse(
                1L,
                "RES-001",
                LocalDate.of(2026, 8, 20),
                "Santa Marta",
                RescueStatus.ADMITTED,
                "DB-CAR",
                "AN-001"
        );
    }
}
