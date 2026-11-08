package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.application.dto.user.UserRequestDTO;
import com.ages.pie.application.dto.user.UserUpdateDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.application.mapper.UserMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.AccountSex;
import com.ages.pie.domain.enums.ProductGender;
import com.ages.pie.infrastructure.auth.SupabaseAdminClient;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductImageRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WishlistItemRepository;
import com.ages.pie.infrastructure.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SexAndGenderServiceTest {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CompanyRepository companyRepository;
    private SupabaseAdminClient supabaseAdminClient;
    private UserService userService;
    private ProductService productService;
    private AuthService authService;
    private ProductMapper productMapper;
    private Company company;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        productRepository = mock(ProductRepository.class);
        companyRepository = mock(CompanyRepository.class);
        supabaseAdminClient = mock(SupabaseAdminClient.class);

        ProductImageRepository imageRepository = mock(ProductImageRepository.class);
        when(imageRepository.findByProductIdOrderByDisplayOrderAsc(any())).thenReturn(List.of());

        productMapper = new ProductMapper(imageRepository);
        userService = new UserService(userRepository, new UserMapper(), supabaseAdminClient);
        productService = new ProductService(
                productRepository,
                companyRepository,
                productMapper,
                mock(WishlistRepository.class),
                mock(WishlistItemRepository.class),
                userRepository,
                mock(RecommendationService.class),
                mock(PreferenceService.class)
        );
        authService = new AuthService(
                mock(SupabaseAuthClient.class),
                userRepository,
                companyRepository
        );
        company = new Company(
                UUID.randomUUID(), "Loja", "12345678000199", "Loja LTDA",
                "Ana", "loja@example.com", UUID.randomUUID(), null, null
        );
    }

    @ParameterizedTest
    @EnumSource(AccountSex.class)
    @NullSource
    void createUser_shouldMapSexToSavedEntityAndResponse(AccountSex sex) {
        when(supabaseAdminClient.createUser("ana@example.com", "senha123"))
                .thenReturn(UUID.randomUUID());
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.create(
                new UserRequestDTO("Ana", "ana@example.com", "senha123", sex)
        );

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getSex()).isEqualTo(sex);
        assertThat(response.sex()).isEqualTo(sex);
    }

    @ParameterizedTest
    @EnumSource(AccountSex.class)
    void updateUser_shouldSaveSelectedSex(AccountSex sex) {
        UUID id = UUID.randomUUID();
        User user = new User("Ana", "ana@example.com", UUID.randomUUID());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.update(id, new UserUpdateDTO("Ana Santos", null, sex));

        verify(userRepository).save(user);
        assertThat(user.getSex()).isEqualTo(sex);
        assertThat(response.sex()).isEqualTo(sex);
        assertThat(response.name()).isEqualTo("Ana Santos");
    }

    @Test
    void updateUser_shouldPreserveSexWhenNotProvided() {
        UUID id = UUID.randomUUID();
        User user = new User("Ana", "ana@example.com", UUID.randomUUID());
        user.updateSex(AccountSex.FEMININO);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.update(id, new UserUpdateDTO("Ana Santos", null, null));

        assertThat(response.sex()).isEqualTo(AccountSex.FEMININO);
        assertThat(response.name()).isEqualTo("Ana Santos");
    }

    @ParameterizedTest
    @EnumSource(ProductGender.class)
    @NullSource
    void createProduct_shouldMapGenderToSavedEntityAndResponse(ProductGender gender) {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.create(new ProductRequestDTO(
                "Camiseta", null, null, null, null, null, null,
                new BigDecimal("49.90"), null, null, company.getId(), gender
        ));

        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        assertThat(saved.getValue().getGender()).isEqualTo(gender);
        assertThat(response.gender()).isEqualTo(gender);
        assertThat(productMapper.toCatalogItemDTO(saved.getValue()).gender()).isEqualTo(gender);
        assertThat(productMapper.toPublicDetailDTO(saved.getValue(), false).gender()).isEqualTo(gender);
    }

    @ParameterizedTest
    @EnumSource(ProductGender.class)
    void updateProduct_shouldSaveSelectedGender(ProductGender gender) {
        UUID id = UUID.randomUUID();
        Product product = new Product(company, "Camiseta");
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        var response = productService.update(id, new ProductUpdateDTO(
                "Camiseta nova", null, null, null, null, null, null,
                null, null, null, null, gender
        ));

        verify(productRepository).save(product);
        assertThat(response.gender()).isEqualTo(gender);
        assertThat(response.name()).isEqualTo("Camiseta nova");
    }

    @Test
    void updateProduct_shouldPreserveGenderWhenNotProvided() {
        UUID id = UUID.randomUUID();
        Product product = new Product(company, "Camiseta");
        product.setGender(ProductGender.UNISSEX);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        var response = productService.update(id, new ProductUpdateDTO(
                "Camiseta nova", null, null, null, null, null, null,
                null, null, null, null, null
        ));

        assertThat(response.gender()).isEqualTo(ProductGender.UNISSEX);
        assertThat(response.name()).isEqualTo("Camiseta nova");
    }

    @ParameterizedTest
    @EnumSource(AccountSex.class)
    @NullSource
    void authSummary_shouldReturnCustomerSex(AccountSex sex) {
        UUID id = UUID.randomUUID();
        User user = new User("Ana", "ana@example.com", UUID.randomUUID());
        user.updateSex(sex);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThat(authService.findSummary(id).sex()).isEqualTo(sex);
    }

    @Test
    void authSummary_shouldReturnNullSexForCompany() {
        UUID id = company.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));

        assertThat(authService.findSummary(id).sex()).isNull();
    }
}