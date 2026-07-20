package com.eai.application.lead;

import com.eai.application.common.ForbiddenException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadServiceTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID OTHER_STORE_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID OTHER_SELLER_ID = UUID.randomUUID();

    @DisplayName("Listagem de vendedor aplica escopo da loja e visibilidade propria antes da paginacao")
    @Test
    void listLeadsAppliesSellerVisibilityBeforePagination() {
        CapturingLeadRepository repository = new CapturingLeadRepository(Optional.empty());
        LeadService service = service(repository);

        service.listLeads(emptyCriteria(), 0, 20, seller());

        assertThat(repository.capturedCriteria.scopeCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(repository.capturedCriteria.scopeStoreId()).isEqualTo(STORE_ID);
        assertThat(repository.capturedCriteria.visibleToSellerUserId()).isEqualTo(SELLER_ID);
    }

    @DisplayName("Listagem de gerente aplica escopo da empresa sem restringir loja")
    @Test
    void listLeadsAppliesManagerCompanyScope() {
        CapturingLeadRepository repository = new CapturingLeadRepository(Optional.empty());
        LeadService service = service(repository);

        service.listLeads(emptyCriteria(), 0, 20, manager());

        assertThat(repository.capturedCriteria.scopeCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(repository.capturedCriteria.scopeStoreId()).isNull();
        assertThat(repository.capturedCriteria.visibleToSellerUserId()).isNull();
    }

    @DisplayName("Listagem de gerente de loja aplica escopo da loja sem restringir vendedor")
    @Test
    void listLeadsAppliesStoreManagerStoreScope() {
        CapturingLeadRepository repository = new CapturingLeadRepository(Optional.empty());
        LeadService service = service(repository);

        service.listLeads(emptyCriteria(), 0, 20, storeManager());

        assertThat(repository.capturedCriteria.scopeCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(repository.capturedCriteria.scopeStoreId()).isEqualTo(STORE_ID);
        assertThat(repository.capturedCriteria.visibleToSellerUserId()).isNull();
    }

    @DisplayName("Vendedor acessa lead disponivel sem dono")
    @Test
    void sellerCanAccessAvailableUnassignedLead() {
        Lead lead = lead(LeadStatus.AVAILABLE, null, STORE_ID, COMPANY_ID);
        LeadService service = service(new CapturingLeadRepository(Optional.of(lead)));

        Lead result = service.getLead(lead.getId(), seller());

        assertThat(result).isSameAs(lead);
    }

    @DisplayName("Vendedor acessa lead sob sua responsabilidade")
    @Test
    void sellerCanAccessOwnLead() {
        Lead lead = lead(LeadStatus.ASSIGNED, SELLER_ID, STORE_ID, COMPANY_ID);
        LeadService service = service(new CapturingLeadRepository(Optional.of(lead)));

        Lead result = service.getLead(lead.getId(), seller());

        assertThat(result).isSameAs(lead);
    }

    @DisplayName("Vendedor nao acessa lead de outro vendedor")
    @Test
    void sellerCannotAccessOtherSellerLead() {
        Lead lead = lead(LeadStatus.ASSIGNED, OTHER_SELLER_ID, STORE_ID, COMPANY_ID);
        LeadService service = service(new CapturingLeadRepository(Optional.of(lead)));

        assertThatThrownBy(() -> service.getLead(lead.getId(), seller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Gerente acessa lead de outra loja da mesma empresa")
    @Test
    void managerCanAccessLeadFromAnotherStoreInCompany() {
        Lead lead = lead(LeadStatus.ASSIGNED, OTHER_SELLER_ID, OTHER_STORE_ID, COMPANY_ID);
        LeadService service = service(new CapturingLeadRepository(Optional.of(lead)));

        Lead result = service.getLead(lead.getId(), manager());

        assertThat(result).isSameAs(lead);
    }

    @DisplayName("Gerente de loja nao acessa lead de outra loja")
    @Test
    void storeManagerCannotAccessLeadFromAnotherStore() {
        Lead lead = lead(LeadStatus.ASSIGNED, OTHER_SELLER_ID, OTHER_STORE_ID, COMPANY_ID);
        LeadService service = service(new CapturingLeadRepository(Optional.of(lead)));

        assertThatThrownBy(() -> service.getLead(lead.getId(), storeManager()))
                .isInstanceOf(ForbiddenException.class);
    }

    private LeadService service(LeadRepository leadRepository) {
        return new LeadService(leadRepository, null, null, null, null, null, null, null, null);
    }

    private LeadSearchCriteria emptyCriteria() {
        return new LeadSearchCriteria(null, null, null, null, null, null, null, null, null, null, null);
    }

    private AuthenticatedUser seller() {
        return new AuthenticatedUser(SELLER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(UUID.randomUUID(), "manager@eai.com", COMPANY_ID, null, Set.of(UserRole.MANAGER));
    }

    private AuthenticatedUser storeManager() {
        return new AuthenticatedUser(UUID.randomUUID(), "store.manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.STORE_MANAGER));
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId, UUID storeId, UUID companyId) {
        Instant now = Instant.now();
        return new Lead(
                UUID.randomUUID(),
                companyId,
                storeId,
                "Cliente",
                "+5511999990000",
                null,
                null,
                "Honda Civic",
                LeadSource.MANUAL,
                null,
                status,
                assignedToUserId,
                assignedToUserId == null ? null : now,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }

    private static class CapturingLeadRepository implements LeadRepository {

        private final Optional<Lead> lead;
        private LeadSearchCriteria capturedCriteria;

        CapturingLeadRepository(Optional<Lead> lead) {
            this.lead = lead;
        }

        @Override
        public PageResult<Lead> search(LeadSearchCriteria criteria, int page, int size) {
            this.capturedCriteria = criteria;
            return new PageResult<>(List.of(), page, size, 0, 0);
        }

        @Override
        public List<Lead> findAll(LeadSearchCriteria criteria) {
            this.capturedCriteria = criteria;
            return lead.stream().toList();
        }

        @Override
        public Optional<Lead> findById(UUID id) {
            return lead.filter(item -> item.getId().equals(id));
        }

        @Override
        public Lead save(Lead lead) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Lead> findPendingByStoreId(UUID storeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Lead> findOverdueCandidatesByStoreId(UUID storeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<UUID> findMostRecentAssignedSellerId(UUID storeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Lead> findMostRecentByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countOpenByAssignedToUserId(UUID userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
            throw new UnsupportedOperationException();
        }
    }
}
