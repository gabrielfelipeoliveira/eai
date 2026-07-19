package com.eai.application.lead;

import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineServiceTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID OTHER_SELLER_ID = UUID.randomUUID();

    @DisplayName("Pipeline do vendedor inclui leads sem dono e seus proprios leads")

    @Test
    void sellerPipelineIncludesUnassignedAndOwnAssignedLeadsOnly() {
        Lead unassignedNew = lead(LeadStatus.NEW, null);
        Lead unassignedAvailable = lead(LeadStatus.AVAILABLE, null);
        Lead ownAssigned = lead(LeadStatus.ASSIGNED, SELLER_ID);
        Lead otherAssigned = lead(LeadStatus.ASSIGNED, OTHER_SELLER_ID);
        PipelineService service = new PipelineService(new FakeLeadRepository(List.of(
                unassignedNew,
                unassignedAvailable,
                ownAssigned,
                otherAssigned
        )));

        Map<LeadStatus, List<Lead>> pipeline = service.getPipeline(new AuthenticatedUser(
                SELLER_ID,
                "seller@eai.com",
                COMPANY_ID,
                STORE_ID,
                Set.of(UserRole.SELLER)
        ));

        assertThat(pipeline.get(LeadStatus.NEW)).containsExactly(unassignedNew);
        assertThat(pipeline.get(LeadStatus.AVAILABLE)).containsExactly(unassignedAvailable);
        assertThat(pipeline.get(LeadStatus.ASSIGNED)).containsExactly(ownAssigned);
        assertThat(pipeline).containsOnlyKeys(LeadStatus.values());
        assertThat(pipeline.get(LeadStatus.SIMULATING)).isEmpty();
        assertThat(pipeline.get(LeadStatus.PROPOSAL_APPROVED)).isEmpty();
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId) {
        Instant now = Instant.now();
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
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

    private record FakeLeadRepository(List<Lead> leads) implements LeadRepository {

        @Override
        public PageResult<Lead> search(LeadSearchCriteria criteria, int page, int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Lead> findAll(LeadSearchCriteria criteria) {
            return leads;
        }

        @Override
        public Optional<Lead> findById(UUID id) {
            throw new UnsupportedOperationException();
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
        public long countOpenByAssignedToUserId(UUID userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Lead> findMostRecentByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
            throw new UnsupportedOperationException();
        }
    }
}
