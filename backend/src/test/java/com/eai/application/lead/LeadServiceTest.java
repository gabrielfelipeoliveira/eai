package com.eai.application.lead;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.conversation.ConversationRepository;
import com.eai.application.item.ItemRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.conversation.Conversation;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadNote;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.lead.LeadTag;
import com.eai.domain.lead.LeadTagDefinition;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @DisplayName("Criacao normaliza telefones adicionais e registra historico de lead novo")
    @Test
    void createLeadNormalizesPhonesAndStoresHistory() {
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
        LeadService service = serviceWithMocks(leadRepository, historyRepository, null, null, null, null, null, null);
        when(leadRepository.findMostRecentByStoreIdAndAnyPhone(any(), any())).thenReturn(Optional.empty());
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead saved = service.createLead(new CreateLeadCommand(
                COMPANY_ID,
                STORE_ID,
                " Cliente ",
                "(11) 99999-0000",
                List.of("(11) 99999-0000", "11988880000", "11988880000"),
                "cliente@eai.com",
                "Sao Paulo",
                "Civic",
                null,
                "Mensagem original",
                null,
                null,
                BigDecimal.TEN,
                "BRL",
                null
        ), manager());

        assertThat(saved.getSource()).isEqualTo(LeadSource.MANUAL);
        assertThat(saved.getCustomerPhone()).isEqualTo("+5511999990000");
        assertThat(saved.getAdditionalPhones()).containsExactly("+5511988880000");
        verify(historyRepository).save(any(LeadHistory.class));
    }

    @DisplayName("Criacao marca lead como duplicado quando existe telefone recente na mesma loja")
    @Test
    void createLeadMarksDuplicatedWhenPhoneAlreadyExists() {
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
        Lead existing = lead(LeadStatus.ASSIGNED, SELLER_ID, STORE_ID, COMPANY_ID);
        LeadService service = serviceWithMocks(leadRepository, historyRepository, null, null, null, null, null, null);
        when(leadRepository.findMostRecentByStoreIdAndAnyPhone(any(), any())).thenReturn(Optional.of(existing));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead saved = service.createLead(new CreateLeadCommand(
                COMPANY_ID, STORE_ID, "Cliente", "+5511999990000", List.of(),
                null, null, null, LeadSource.WEBSITE, null, null, null, null, null, null
        ), manager());

        assertThat(saved.getStatus()).isEqualTo(LeadStatus.DUPLICATED);
        assertThat(saved.getRelatedLeadId()).isEqualTo(existing.getId());
    }

    @DisplayName("Atualizacao troca responsavel e sincroniza conversa vinculada")
    @Test
    void updateLeadSyncsConversationOwnerWhenAssignmentChanges() {
        Lead lead = lead(LeadStatus.NEW, null, STORE_ID, COMPANY_ID);
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        Conversation conversation = Conversation.create(COMPANY_ID, STORE_ID, UUID.randomUUID(), lead.getId(), null);
        LeadService service = serviceWithMocks(leadRepository, historyRepository, null, null, userRepository, null, null, conversationRepository);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(user(SELLER_ID, UserRole.SELLER, UserStatus.ACTIVE)));
        when(conversationRepository.findByLeadId(lead.getId())).thenReturn(Optional.of(conversation));

        Lead updated = service.updateLead(lead.getId(), new UpdateLeadCommand(
                COMPANY_ID, STORE_ID, "Cliente atualizado", "+5511977770000", List.of(),
                "cliente@eai.com", "Sao Paulo", "Corolla", LeadSource.MANUAL,
                "Texto", LeadStatus.ASSIGNED, SELLER_ID, null, null, null, null, null, null
        ), manager());

        assertThat(updated.getAssignedToUserId()).isEqualTo(SELLER_ID);
        assertThat(conversation.getResponsibleUserId()).isEqualTo(SELLER_ID);
        verify(conversationRepository).save(conversation);
        verify(historyRepository).save(any(LeadHistory.class));
    }

    @DisplayName("Atribuicao direta rejeita usuario fora da loja do lead")
    @Test
    void assignRejectsUserFromDifferentStore() {
        Lead lead = lead(LeadStatus.NEW, null, STORE_ID, COMPANY_ID);
        LeadRepository leadRepository = mock(LeadRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LeadService service = serviceWithMocks(leadRepository, null, null, null, userRepository, null, null, null);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(userRepository.findById(OTHER_SELLER_ID)).thenReturn(Optional.of(user(OTHER_SELLER_ID, UserRole.SELLER, UserStatus.ACTIVE, COMPANY_ID, OTHER_STORE_ID)));

        assertThatThrownBy(() -> service.assign(lead.getId(), OTHER_SELLER_ID, manager()))
                .isInstanceOf(ForbiddenException.class);
        verify(leadRepository, never()).save(any());
    }

    @DisplayName("Vendedor nao assume lead de outro vendedor quando nao tem acesso ao lead")
    @Test
    void assignToMeRejectsSellerTakeover() {
        Lead lead = lead(LeadStatus.ASSIGNED, OTHER_SELLER_ID, STORE_ID, COMPANY_ID);
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadService service = serviceWithMocks(leadRepository, null, null, null, null, null, null, null);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> service.assignToMe(lead.getId(), seller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Atualizacao de nota valida vinculo com o lead antes de salvar")
    @Test
    void updateNoteRejectsNoteFromAnotherLead() {
        Lead lead = lead(LeadStatus.NEW, null, STORE_ID, COMPANY_ID);
        LeadNoteRepository noteRepository = mock(LeadNoteRepository.class);
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadService service = serviceWithMocks(leadRepository, null, noteRepository, null, null, null, null, null);
        UUID noteId = UUID.randomUUID();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(LeadNote.create(UUID.randomUUID(), SELLER_ID, "Nota")));

        assertThatThrownBy(() -> service.updateNote(lead.getId(), noteId, "Nova nota", manager()))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("Inclusao de tag por nome impede duplicidade por tipo")
    @Test
    void addTagRejectsDuplicateType() {
        Lead lead = lead(LeadStatus.NEW, null, STORE_ID, COMPANY_ID);
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadTagRepository tagRepository = mock(LeadTagRepository.class);
        LeadTagDefinitionRepository tagDefinitionRepository = mock(LeadTagDefinitionRepository.class);
        LeadTagDefinition definition = new LeadTagDefinition(UUID.randomUUID(), "Quente", "TEMPERATURA", true, Instant.now(), Instant.now());
        LeadService service = serviceWithMocks(leadRepository, null, null, tagRepository, null, tagDefinitionRepository, null, null);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(tagDefinitionRepository.findActiveByName("Quente")).thenReturn(Optional.of(definition));
        when(tagRepository.existsByLeadIdAndType(lead.getId(), "TEMPERATURA")).thenReturn(true);

        assertThatThrownBy(() -> service.addTag(lead.getId(), null, "Quente", manager()))
                .isInstanceOf(ConflictException.class);
        verify(tagRepository, never()).save(any(LeadTag.class));
    }

    @DisplayName("Remocao de tag valida vinculo com o lead antes de excluir")
    @Test
    void deleteTagRejectsTagFromAnotherLead() {
        Lead lead = lead(LeadStatus.NEW, null, STORE_ID, COMPANY_ID);
        LeadRepository leadRepository = mock(LeadRepository.class);
        LeadTagRepository tagRepository = mock(LeadTagRepository.class);
        LeadService service = serviceWithMocks(leadRepository, null, null, tagRepository, null, null, null, null);
        UUID tagId = UUID.randomUUID();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(new LeadTag(tagId, UUID.randomUUID(), UUID.randomUUID(), "Quente", "TEMPERATURA")));

        assertThatThrownBy(() -> service.deleteTag(lead.getId(), tagId, manager()))
                .isInstanceOf(NotFoundException.class);
        verify(tagRepository, never()).deleteById(any());
    }

    private LeadService service(LeadRepository leadRepository) {
        return new LeadService(leadRepository, null, null, null, null, null, null, null, null, mock(ConversationRepository.class));
    }

    private LeadService serviceWithMocks(
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            LeadNoteRepository noteRepository,
            LeadTagRepository tagRepository,
            UserRepository userRepository,
            LeadTagDefinitionRepository tagDefinitionRepository,
            ItemRepository itemRepository,
            ConversationRepository conversationRepository
    ) {
        CompanyService companyService = mock(CompanyService.class);
        StoreService storeService = mock(StoreService.class);
        when(storeService.findRequired(STORE_ID)).thenReturn(store(STORE_ID, COMPANY_ID));
        when(storeService.findRequired(OTHER_STORE_ID)).thenReturn(store(OTHER_STORE_ID, COMPANY_ID));
        return new LeadService(
                leadRepository,
                historyRepository == null ? mock(LeadHistoryRepository.class) : historyRepository,
                noteRepository == null ? mock(LeadNoteRepository.class) : noteRepository,
                tagRepository == null ? mock(LeadTagRepository.class) : tagRepository,
                tagDefinitionRepository == null ? mock(LeadTagDefinitionRepository.class) : tagDefinitionRepository,
                companyService,
                storeService,
                userRepository == null ? mock(UserRepository.class) : userRepository,
                itemRepository == null ? mock(ItemRepository.class) : itemRepository,
                conversationRepository == null ? mock(ConversationRepository.class) : conversationRepository
        );
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

    private User user(UUID id, UserRole role, UserStatus status) {
        return user(id, role, status, COMPANY_ID, STORE_ID);
    }

    private User user(UUID id, UserRole role, UserStatus status, UUID companyId, UUID storeId) {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new User(
                id,
                "Usuario",
                id + "@eai.com",
                "hash",
                null,
                null,
                companyId,
                storeId,
                status,
                Set.of(role),
                now,
                now
        );
    }

    private Store store(UUID id, UUID companyId) {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Store(id, companyId, "Loja", "123", null, null, null, null, null, TenantStatus.ACTIVE, now, now);
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
