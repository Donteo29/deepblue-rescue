package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService treatmentService;

    @Test
    void shouldCreateTreatmentAndReturn201() throws Exception {
        when(treatmentService.register(any(CreateTreatmentRequest.class)))
                .thenReturn(new TreatmentResponse(
                        10L,
                        "AN-001",
                        "SPEC-001",
                        LocalDateTime.of(2026, 8, 21, 9, 30),
                        TreatmentType.WOUND_CARE,
                        "Cleaning and evaluation"
                ));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "animalCode": "AN-001",
                                  "specialistCode": "SPEC-001",
                                  "performedAt": "2026-08-21T09:30:00",
                                  "type": "WOUND_CARE",
                                  "description": "Cleaning and evaluation"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.type").value("WOUND_CARE"));

        verify(treatmentService).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestFailsValidation() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(treatmentService);
    }

    @Test
    void shouldReturn400ForInvalidTreatmentType() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "animalCode": "AN-001",
                                  "specialistCode": "SPEC-001",
                                  "performedAt": "2026-08-21T09:30:00",
                                  "type": "NOT_A_TREATMENT",
                                  "description": "test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(treatmentService);
    }
}
