package com.ages.pie.domain.entity;

import com.ages.pie.domain.enums.CompanyRole;
import com.ages.pie.domain.enums.CompanyStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyAdminTest {

    @Test
    void admin_shouldCreateActiveAdminWithoutStoreData() {
        UUID supabaseUserId = UUID.randomUUID();

        Company admin = Company.admin("Admin Novo", "admin@pie.com", supabaseUserId);

        assertThat(admin.getRole()).isEqualTo(CompanyRole.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(admin.isActive()).isTrue();
        assertThat(admin.getName()).isEqualTo("Admin Novo");
        assertThat(admin.getEmail()).isEqualTo("admin@pie.com");
        assertThat(admin.getSupabaseUserId()).isEqualTo(supabaseUserId);
        assertThat(admin.getCnpj()).isNull();
        assertThat(admin.getSocialReason()).isNull();
        assertThat(admin.getResponsiblePerson()).isNull();
    }

    @Test
    void admin_shouldRejectBlankName() {
        assertThatThrownBy(() -> Company.admin("  ", "admin@pie.com", UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome");
    }

    @Test
    void admin_shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> Company.admin("Admin Novo", "sem-arroba", UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void admin_shouldRequireSupabaseUserId() {
        assertThatThrownBy(() -> Company.admin("Admin Novo", "admin@pie.com", null))
                .isInstanceOf(NullPointerException.class);
    }
}
