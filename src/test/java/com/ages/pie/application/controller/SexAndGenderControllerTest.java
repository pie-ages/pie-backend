package com.ages.pie.application.controller;

import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.application.dto.user.UserRequestDTO;
import com.ages.pie.application.dto.user.UserUpdateDTO;
import com.ages.pie.application.exception.GlobalExceptionHandler;
import com.ages.pie.application.service.ColorimetryService;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.ProductService;
import com.ages.pie.application.service.RecommendationService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.domain.enums.AccountSex;
import com.ages.pie.domain.enums.ProductGender;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SexAndGenderControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ProductService productService;
    private UUID id;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        productService = mock(ProductService.class);
        id = UUID.randomUUID();

        UserController userController = new UserController(
                userService,
                mock(PreferenceService.class),
                mock(ColorimetryService.class),
                mock(StyleIdentificationService.class),
                mock(AuthenticatedUserProvider.class)
        );
        ProductController productController = new ProductController(
                productService,
                mock(RecommendationService.class),
                mock(AuthenticatedUserProvider.class)
        );

        mockMvc = MockMvcBuilders.standaloneSetup(userController, productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @EnumSource(AccountSex.class)
    void userEndpoints_shouldAcceptEachSex(AccountSex sex) throws Exception {
        String body = """
                {"name":"Ana","email":"ana@example.com","password":"senha123","sex":"%s"}
                """.formatted(sex.name());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<UserRequestDTO> created = ArgumentCaptor.forClass(UserRequestDTO.class);
        ArgumentCaptor<UserUpdateDTO> updated = ArgumentCaptor.forClass(UserUpdateDTO.class);
        verify(userService).create(created.capture());
        verify(userService).update(eq(id), updated.capture());
        assertThat(created.getValue().sex()).isEqualTo(sex);
        assertThat(updated.getValue().sex()).isEqualTo(sex);
    }

    @ParameterizedTest
    @EnumSource(ProductGender.class)
    void productEndpoints_shouldAcceptEachGender(ProductGender gender) throws Exception {
        String body = """
                {"name":"Camiseta","price":49.90,"companyId":"%s","gender":"%s"}
                """.formatted(id, gender.name());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<ProductRequestDTO> created = ArgumentCaptor.forClass(ProductRequestDTO.class);
        ArgumentCaptor<ProductUpdateDTO> updated = ArgumentCaptor.forClass(ProductUpdateDTO.class);
        verify(productService).create(created.capture());
        verify(productService).update(eq(id), updated.capture());
        assertThat(created.getValue().gender()).isEqualTo(gender);
        assertThat(updated.getValue().gender()).isEqualTo(gender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"INVALIDO\"", "\"\"", "0", "true", "\"UNISSEX\""})
    void userEndpoints_shouldRejectInvalidSex(String value) throws Exception {
        String body = """
                {"name":"Ana","email":"ana@example.com","password":"senha123","sex":%s}
                """.formatted(value);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"INVALIDO\"", "\"\"", "0", "true", "\"PREFIRO_NAO_INFORMAR\""})
    void productEndpoints_shouldRejectInvalidGender(String value) throws Exception {
        String body = """
                {"name":"Camiseta","price":49.90,"companyId":"%s","gender":%s}
                """.formatted(id, value);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"sex\":null"})
    void userEndpoints_shouldAcceptMissingOrNullSex(String field) throws Exception {
        String body = """
                {"name":"Ana","email":"ana@example.com","password":"senha123"%s}
                """.formatted(field);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<UserRequestDTO> created = ArgumentCaptor.forClass(UserRequestDTO.class);
        ArgumentCaptor<UserUpdateDTO> updated = ArgumentCaptor.forClass(UserUpdateDTO.class);
        verify(userService).create(created.capture());
        verify(userService).update(eq(id), updated.capture());
        assertThat(created.getValue().sex()).isNull();
        assertThat(updated.getValue().sex()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"gender\":null"})
    void productEndpoints_shouldAcceptMissingOrNullGender(String field) throws Exception {
        String body = """
                {"name":"Camiseta","price":49.90,"companyId":"%s"%s}
                """.formatted(id, field);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<ProductRequestDTO> created = ArgumentCaptor.forClass(ProductRequestDTO.class);
        ArgumentCaptor<ProductUpdateDTO> updated = ArgumentCaptor.forClass(ProductUpdateDTO.class);
        verify(productService).create(created.capture());
        verify(productService).update(eq(id), updated.capture());
        assertThat(created.getValue().gender()).isNull();
        assertThat(updated.getValue().gender()).isNull();
    }
}