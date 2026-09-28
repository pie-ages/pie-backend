package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.dto.style.StyleOptionResponseDTO;
import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.service.StyleService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(StyleController.class)
class StyleControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean StyleService styleService;
    @MockitoBean AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    void questionsReturnPublicImageUrlsForDisplayedPair() throws Exception {
        UUID questionId = UUID.randomUUID();
        String baseUrl = "https://fjpdxidknltrczqqwbas.supabase.co/storage/v1/object/public/style-quiz/";
        when(styleService.findQuestions()).thenReturn(List.of(new StyleQuestionResponseDTO(
                questionId, "Qual look você usaria?", 1, List.of(
                        new StyleOptionResponseDTO(UUID.randomUUID(), "Alfaiataria Chic", baseUrl + "q1-a.png", "ELEGANTE", 1),
                        new StyleOptionResponseDTO(UUID.randomUUID(), "Casual Alinhado", baseUrl + "q1-b.png", "CASUAL", 2)))));

        mockMvc.perform(get("/api/style/questions").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].question").value("Qual look você usaria?"))
                .andExpect(jsonPath("$.questions[0].order").value(1))
                .andExpect(jsonPath("$.questions[0].options[0].imageUrl").value(baseUrl + "q1-a.png"))
                .andExpect(jsonPath("$.questions[0].options[1].imageUrl").value(baseUrl + "q1-b.png"));
    }

    @Test
    void rejectsWritesToAnotherProfile() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(UUID.randomUUID());
        mockMvc.perform(post("/users/{userId}/style/answers", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":\"" + UUID.randomUUID()
                                + "\",\"answerType\":\"NONE\"}]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitsAnswersForAuthenticatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(styleService.submitAnswers(org.mockito.ArgumentMatchers.eq(userId), anyList()))
                .thenReturn(new StyleResultResponseDTO(List.of()));
        mockMvc.perform(post("/users/me/style/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":\"" + UUID.randomUUID()
                                + "\",\"answerType\":\"NONE\"}]}"))
                .andExpect(status().isOk());
        verify(styleService).submitAnswers(org.mockito.ArgumentMatchers.eq(userId), anyList());
    }

    @Test
    void rejectsProfileReadWithoutToken() throws Exception {
        when(authenticatedUserProvider.id()).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        mockMvc.perform(get("/users/me/style"))
                .andExpect(status().isUnauthorized());
    }

        @Test
        void rejectsProfileUpdateWithoutToken() throws Exception {
                when(authenticatedUserProvider.id()).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                mockMvc.perform(put("/users/me/style")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"styles\":[\"CASUAL\"]}"))
                                .andExpect(status().isUnauthorized());
                org.mockito.Mockito.verifyNoInteractions(styleService);
        }

    @Test
    void updatesAndReadsOwnStyles() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(styleService.updateStyles(org.mockito.ArgumentMatchers.eq(userId), anyList()))
                .thenReturn(new StyleResultResponseDTO(List.of("CASUAL", "BOHO")));
        when(styleService.getStyles(userId)).thenReturn(new StyleResultResponseDTO(List.of("CASUAL", "BOHO")));

        mockMvc.perform(put("/users/me/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"styles\":[\"CASUAL\",\"BOHO\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles[0]").value("CASUAL"));
        mockMvc.perform(get("/users/me/style"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles[1]").value("BOHO"));
    }

    @Test
    void rejectsInvalidStyle() throws Exception {
        mockMvc.perform(put("/users/me/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"styles\":[\"CLASSIC\"]}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(styleService);
    }

    @Test
    void reportsMissingAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(styleService.getStyles(userId)).thenThrow(new ResourceNotFoundException("Usuário não encontrado"));
        mockMvc.perform(get("/users/me/style"))
                .andExpect(status().isNotFound());
    }

}