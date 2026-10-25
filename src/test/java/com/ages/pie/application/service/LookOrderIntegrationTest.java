package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookItemRefDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookUpdateDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WardrobeItemRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante que a sequência de peças enviada é idêntica à persistida e à consultada,
 * inclusive após limpar o contexto de persistência (equivalente a reabrir o app).
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class LookOrderIntegrationTest {

    @Autowired
    private LookService lookService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WardrobeItemRepository wardrobeItemRepository;
    @Autowired
    private EntityManager entityManager;

    private User user;
    private Company company;

    @BeforeEach
    void seed() {
        user = userRepository.save(new User("Ordem", "ordem-" + UUID.randomUUID() + "@teste.com", UUID.randomUUID()));
        company = companyRepository.save(new Company(null, "Loja", unique14(), "Loja LTDA", "Maria",
                "loja-" + UUID.randomUUID() + "@teste.com", UUID.randomUUID(), null, null));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5 })
    void create_shouldPreserveInterleavedOrder_afterSaveAndReload(int size) {
        List<LookItemRefDTO> refs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            // alterna produto / peça do guarda-roupa para provar que o tipo não agrupa a lista
            refs.add(i % 2 == 0 ? new LookItemRefDTO(null, newProduct("Produto " + i).getId())
                                : new LookItemRefDTO(newWardrobeItem("Peça " + i).getId(), null));
        }

        LookResponseDTO created = lookService.create(user.getId(), new LookRequestDTO(
                "Look", null, null, null, null, refs));
        entityManager.flush();
        entityManager.clear();

        assertThat(keys(created.items())).isEqualTo(keys(refs));
        assertThat(keys(lookService.findById(user.getId(), created.id()).items())).isEqualTo(keys(refs));
        assertThat(keys(lookService.findAllByUser(user.getId()).get(0).items())).isEqualTo(keys(refs));
    }

    @Test
    void update_shouldKeepOrder_andAddedPieceGoesLast() {
        UUID casaco = newProduct("Casaco").getId();
        UUID camisa = newWardrobeItem("Camisa").getId();
        UUID calca = newWardrobeItem("Calça").getId();
        LookResponseDTO created = lookService.create(user.getId(), new LookRequestDTO("Look", null, null, null, null,
                List.of(new LookItemRefDTO(null, casaco), new LookItemRefDTO(camisa, null),
                        new LookItemRefDTO(calca, null))));

        lookService.update(user.getId(), created.id(), new LookUpdateDTO("Renomeado", null, null));
        UUID tenis = newProduct("Tênis").getId();
        lookService.addProduct(user.getId(), created.id(), tenis);
        entityManager.flush();
        entityManager.clear();

        assertThat(keys(lookService.findById(user.getId(), created.id()).items()))
                .containsExactly(casaco, camisa, calca, tenis);
    }

    private Product newProduct(String name) {
        return productRepository.save(new Product(company, name));
    }

    private WardrobeItem newWardrobeItem(String name) {
        return wardrobeItemRepository.save(new WardrobeItem(user, null, name, "Categoria", "casual", "Preto"));
    }

    private static List<UUID> keys(List<?> items) {
        return items.stream().map(i -> {
            if (i instanceof LookItemDTO dto) {
                return dto.wardrobeItemId() != null ? dto.wardrobeItemId() : dto.productId();
            }
            LookItemRefDTO ref = (LookItemRefDTO) i;
            return ref.wardrobeItemId() != null ? ref.wardrobeItemId() : ref.productId();
        }).toList();
    }

    private static String unique14() {
        return String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100_000_000_000_000L);
    }
}
