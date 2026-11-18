package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.CatalogFiltersDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WishlistItemRepository;
import com.ages.pie.infrastructure.repository.WishlistRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCatalogTransactionTest {
    @Test
    void recommendationRepositoryFailureRollsBackOnlyItsTransactionAndCatalogStillCommits() {
        // Use real Spring transaction interception, without requiring a database for this regression.
        var emf = mock(EntityManagerFactory.class);
        List<EntityTransaction> transactions = new ArrayList<>();
        when(emf.createEntityManager()).thenAnswer(invocation -> entityManager(transactions));
        var transactionManager = new JpaTransactionManager(emf);

        UUID userId = UUID.randomUUID();
        var rawUsers = mock(UserRepository.class);
        when(rawUsers.findById(userId)).thenThrow(new DataAccessResourceFailureException("Profile query failed"));
        var required = new RuleBasedTransactionAttribute();
        required.setReadOnly(true);
        var repositoryAttributes = new NameMatchTransactionAttributeSource();
        repositoryAttributes.addTransactionalMethod("*", required);
        var repositoryFactory = new ProxyFactory(rawUsers);
        repositoryFactory.setInterfaces(UserRepository.class);
        repositoryFactory.addAdvice(new TransactionInterceptor(transactionManager, repositoryAttributes));
        var users = (UserRepository) repositoryFactory.getProxy();

        var products = mock(ProductRepository.class);
        var mapper = new ProductMapper(null);
        var recommendations = transactionalProxy(new RecommendationService(users, mock(BodyProfileRepository.class),
                products, (styles, colors, product) -> 1.0, mapper), transactionManager);
        var service = transactionalProxy(new ProductService(products, mock(CompanyRepository.class), mapper,
                mock(WishlistRepository.class), mock(WishlistItemRepository.class), users, recommendations, mock(PreferenceService.class)), transactionManager);
        var fallback = mock(Product.class);
        when(fallback.getName()).thenReturn("Fallback disponível");
        var pageable = PageRequest.of(0, 20);
        when(products.findCatalog(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(fallback), pageable, 1));

        var result = service.findCatalog(null, new CatalogFiltersDTO(null, null, null, null, null),
                pageable, Optional.of(userId));

        assertThat(result.items()).extracting(item -> item.name()).containsExactly("Fallback disponível");
        assertThat(transactions).hasSize(2);
        verify(transactions.get(1)).rollback();
        verify(transactions.getFirst()).commit();
        verify(transactions.getFirst(), never()).setRollbackOnly();
    }

    private static EntityManager entityManager(List<EntityTransaction> transactions) {
        var em = mock(EntityManager.class);
        var tx = mock(EntityTransaction.class);
        var active = new AtomicBoolean();
        var rollbackOnly = new AtomicBoolean();
        when(em.isOpen()).thenReturn(true);
        when(em.getTransaction()).thenReturn(tx);
        when(tx.isActive()).thenAnswer(i -> active.get());
        when(tx.getRollbackOnly()).thenAnswer(i -> rollbackOnly.get());
        doAnswer(i -> { active.set(true); return null; }).when(tx).begin();
        doAnswer(i -> { active.set(false); return null; }).when(tx).commit();
        doAnswer(i -> { active.set(false); return null; }).when(tx).rollback();
        doAnswer(i -> { rollbackOnly.set(true); return null; }).when(tx).setRollbackOnly();
        transactions.add(tx);
        return em;
    }

    @SuppressWarnings("unchecked")
    private static <T> T transactionalProxy(T target, JpaTransactionManager transactionManager) {
        var factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }
}
