package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerServiceErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService treatmentService;

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(treatmentService.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Animal not found: AN-999"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("AN-999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"));
    }

    @Test
    void shouldReturn409WhenBusinessRulePreventsTreatment() throws Exception {
        when(treatmentService.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new BusinessRuleException("The specialist is not active"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("AN-001")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The specialist is not active"));
    }

    private String validRequest(String animalCode) {
        return """
                {
                  "animalCode": "%s",
                  "specialistCode": "SPEC-001",
                  "performedAt": "2026-08-21T09:30:00",
                  "type": "WOUND_CARE",
                  "description": "test"
                }
                """.formatted(animalCode);
    }
}
