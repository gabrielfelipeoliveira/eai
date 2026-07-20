package com.eai.application.lead;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.item.ItemRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.domain.item.Item;
import com.eai.domain.item.Vehicle;
import com.eai.application.user.UserRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadNote;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.lead.LeadTag;
import com.eai.domain.lead.LeadTagDefinition;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    private static final List<LeadStatus> SELLER_AVAILABLE_STATUSES = List.of(LeadStatus.NEW, LeadStatus.AVAILABLE);

    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final LeadNoteRepository noteRepository;
    private final LeadTagRepository tagRepository;
    private final LeadTagDefinitionRepository tagDefinitionRepository;
    private final CompanyService companyService;
    private final StoreService storeService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public LeadService(
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            LeadNoteRepository noteRepository,
            LeadTagRepository tagRepository,
            LeadTagDefinitionRepository tagDefinitionRepository,
            CompanyService companyService,
            StoreService storeService,
            UserRepository userRepository,
            ItemRepository itemRepository
    ) {
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.companyService = companyService;
        this.storeService = storeService;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Lead> listLeads(LeadSearchCriteria criteria, int page, int size, AuthenticatedUser authenticatedUser) {
        LeadSearchCriteria scopedCriteria = applyScope(criteria, authenticatedUser);
        return leadRepository.search(scopedCriteria, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @Transactional(readOnly = true)
    public Lead getLead(UUID id, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        return lead;
    }

    @Transactional
    public Lead createLead(CreateLeadCommand command, AuthenticatedUser authenticatedUser) {
        validateTenant(command.companyId(), command.storeId());
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        if (command.assignedToUserId() != null) {
            assertCanAssignUser(command.assignedToUserId(), command.companyId(), command.storeId(), authenticatedUser);
        }
        LeadSource source = command.source() == null ? LeadSource.MANUAL : command.source();
        String normalizedPhone = PhoneNormalizer.normalize(command.customerPhone());
        List<String> normalizedAdditionalPhones = normalizeAdditionalPhones(command.additionalPhones(), normalizedPhone);
        ItemRepository.ItemWithVehicle itemWithVehicle = createItemWithVehicle(authenticatedUser.id(), command.item());
        Lead lead = Lead.create(
                command.companyId(),
                command.storeId(),
                command.customerName(),
                normalizedPhone,
                normalizedAdditionalPhones,
                command.customerEmail(),
                command.customerCity(),
                command.vehicleInterest(),
                itemWithVehicle == null ? null : itemWithVehicle.item().getId(),
                itemWithVehicle == null ? null : itemWithVehicle.item(),
                source,
                command.originalMessage(),
                command.assignedToUserId(),
                command.lostReason(),
                command.saleValue(),
                command.saleCurrency()
        );
        leadRepository.findMostRecentByStoreIdAndAnyPhone(command.storeId(), allPhones(normalizedPhone, normalizedAdditionalPhones))
                .ifPresent(existingLead -> lead.markDuplicated(existingLead.getId()));
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(
                savedLead.getId(),
                authenticatedUser.id(),
                null,
                savedLead.getStatus(),
                savedLead.getStatus() == LeadStatus.DUPLICATED
                        ? "Lead criado como duplicado por telefone/WhatsApp na mesma loja"
                        : "Lead created"
        ));
        return savedLead;
    }

    @Transactional
    public Lead updateLead(UUID id, UpdateLeadCommand command, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        validateTenant(command.companyId(), command.storeId());
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        if (command.assignedToUserId() != null) {
            assertCanAssignUser(command.assignedToUserId(), command.companyId(), command.storeId(), authenticatedUser);
        }
        LeadStatus previousStatus = lead.getStatus();
        Instant assignedAt = command.assignedToUserId() == null
                ? null
                : command.assignedToUserId().equals(lead.getAssignedToUserId()) ? lead.getAssignedAt() : Instant.now();
        String normalizedPhone = PhoneNormalizer.normalize(command.customerPhone());
        List<String> normalizedAdditionalPhones = normalizeAdditionalPhones(command.additionalPhones(), normalizedPhone);
        ItemRepository.ItemWithVehicle itemWithVehicle = createItemWithVehicle(authenticatedUser.id(), command.item());
        lead.update(
                command.companyId(),
                command.storeId(),
                command.customerName(),
                normalizedPhone,
                normalizedAdditionalPhones,
                command.customerEmail(),
                command.customerCity(),
                command.vehicleInterest(),
                itemWithVehicle == null ? null : itemWithVehicle.item().getId(),
                itemWithVehicle == null ? null : itemWithVehicle.item(),
                command.source(),
                command.originalMessage(),
                command.status(),
                command.assignedToUserId(),
                assignedAt,
                command.firstContactAt(),
                command.lastContactAt(),
                command.lostReason(),
                command.saleValue(),
                command.saleCurrency()
        );
        Lead savedLead = leadRepository.save(lead);
        if (previousStatus != savedLead.getStatus()) {
            historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Lead updated"));
        }
        return savedLead;
    }

    @Transactional
    public Lead changeStatus(UUID id, LeadStatus status, String description, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        LeadStatus previousStatus = lead.changeStatus(status);
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), description));
        return savedLead;
    }

    @Transactional
    public Lead assignToMe(UUID id, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        if (lead.getAssignedToUserId() != null && !lead.getAssignedToUserId().equals(authenticatedUser.id())) {
            throw new ConflictException("Lead already assigned");
        }
        return assign(id, authenticatedUser.id(), authenticatedUser);
    }

    @Transactional
    public Lead assign(UUID id, UUID userId, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        assertCanAssignUser(userId, lead.getCompanyId(), lead.getStoreId(), authenticatedUser);
        LeadStatus previousStatus = lead.assignTo(userId);
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Lead assigned"));
        return savedLead;
    }

    @Transactional
    public LeadNote addNote(UUID leadId, String note, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        LeadNote savedNote = noteRepository.save(LeadNote.create(lead.getId(), authenticatedUser.id(), note));
        historyRepository.save(LeadHistory.create(lead.getId(), authenticatedUser.id(), lead.getStatus(), lead.getStatus(), "Observacao criada"));
        return savedNote;
    }

    @Transactional
    public LeadNote updateNote(UUID leadId, UUID noteId, String note, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        LeadNote existingNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Lead note not found"));
        if (!existingNote.getLeadId().equals(lead.getId())) {
            throw new NotFoundException("Lead note not found");
        }
        LeadNote updatedNote = noteRepository.save(existingNote.update(note));
        historyRepository.save(LeadHistory.create(lead.getId(), authenticatedUser.id(), lead.getStatus(), lead.getStatus(), "Observacao atualizada"));
        return updatedNote;
    }

    @Transactional(readOnly = true)
    public List<LeadHistory> listHistory(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return historyRepository.findByLeadId(lead.getId());
    }

    @Transactional(readOnly = true)
    public List<LeadNote> listNotes(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return noteRepository.findByLeadId(lead.getId());
    }

    @Transactional
    public LeadTag addTag(UUID leadId, UUID tagId, String name, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        LeadTagDefinition tagDefinition = findTagDefinition(tagId, name);
        if (!tagDefinition.isActive()) {
            throw new ConflictException("Lead tag is inactive");
        }
        if (tagRepository.existsByLeadIdAndTagId(lead.getId(), tagDefinition.getId())) {
            throw new ConflictException("Lead already has this tag");
        }
        if (tagRepository.existsByLeadIdAndType(lead.getId(), tagDefinition.getType())) {
            throw new ConflictException("Lead already has a tag of this type");
        }
        return tagRepository.save(LeadTag.create(lead.getId(), tagDefinition));
    }

    @Transactional(readOnly = true)
    public List<LeadTagDefinition> listTagDefinitions() {
        return tagDefinitionRepository.findAllActive();
    }

    @Transactional
    public LeadTagDefinition createTagDefinition(String name, String type) {
        if (tagDefinitionRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Lead tag name already exists");
        }
        return tagDefinitionRepository.save(LeadTagDefinition.create(name, type));
    }

    @Transactional(readOnly = true)
    public List<LeadTag> listTags(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return tagRepository.findByLeadId(lead.getId());
    }

    @Transactional
    public void deleteTag(UUID leadId, UUID tagId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        LeadTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Lead tag not found"));
        if (!tag.getLeadId().equals(lead.getId())) {
            throw new NotFoundException("Lead tag not found");
        }
        tagRepository.deleteById(tag.getId());
    }

    private List<String> normalizeAdditionalPhones(List<String> additionalPhones, String primaryPhone) {
        if (additionalPhones == null || additionalPhones.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String phone : additionalPhones) {
            String normalizedPhone = PhoneNormalizer.normalize(phone);
            if (normalizedPhone == null || normalizedPhone.equals(primaryPhone) || normalized.contains(normalizedPhone)) {
                continue;
            }
            normalized.add(normalizedPhone);
        }
        return List.copyOf(normalized);
    }

    private List<String> allPhones(String primaryPhone, List<String> additionalPhones) {
        List<String> phones = new ArrayList<>();
        if (primaryPhone != null) {
            phones.add(primaryPhone);
        }
        if (additionalPhones != null) {
            phones.addAll(additionalPhones);
        }
        return List.copyOf(phones);
    }

    private Lead findRequired(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private LeadTagDefinition findTagDefinition(UUID tagId, String name) {
        if (tagId != null) {
            return tagDefinitionRepository.findById(tagId)
                    .orElseThrow(() -> new NotFoundException("Lead tag definition not found"));
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tagId is required");
        }
        return tagDefinitionRepository.findActiveByName(name)
                .orElseThrow(() -> new NotFoundException("Lead tag definition not found"));
    }

    private ItemRepository.ItemWithVehicle createItemWithVehicle(UUID ownerUserId, LeadItemCommand itemCommand) {
        LeadVehicleCommand vehicleCommand = itemCommand == null ? null : itemCommand.vehicle();
        if (!hasItemData(itemCommand) && (vehicleCommand == null || !vehicleCommand.hasData())) {
            return null;
        }
        String itemName = itemCommand == null ? null : itemCommand.name();
        Item item = Item.create(ownerUserId, itemName);
        Vehicle vehicle = vehicleCommand == null || !vehicleCommand.hasData()
                ? null
                : Vehicle.create(item.getId(), vehicleCommand.name(), vehicleCommand.year(), vehicleCommand.model(), vehicleCommand.value());
        return itemRepository.save(item, vehicle);
    }

    private boolean hasItemData(LeadItemCommand itemCommand) {
        return itemCommand != null && itemCommand.name() != null && !itemCommand.name().isBlank();
    }

    private LeadSearchCriteria applyScope(LeadSearchCriteria criteria, AuthenticatedUser authenticatedUser) {
        UUID scopeCompanyId = null;
        UUID scopeStoreId = null;
        UUID visibleToSellerUserId = null;
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            scopeCompanyId = null;
            scopeStoreId = null;
        } else if (hasRole(authenticatedUser, UserRole.MANAGER)) {
            scopeCompanyId = requireCompany(authenticatedUser);
        } else {
            scopeCompanyId = requireCompany(authenticatedUser);
            scopeStoreId = requireStore(authenticatedUser);
            if (hasRole(authenticatedUser, UserRole.SELLER) && !hasRole(authenticatedUser, UserRole.STORE_MANAGER)) {
                visibleToSellerUserId = authenticatedUser.id();
            }
        }
        return new LeadSearchCriteria(
                criteria.status(),
                criteria.source(),
                criteria.assignedToUserId(),
                criteria.storeId(),
                criteria.createdFrom(),
                criteria.createdTo(),
                criteria.text(),
                criteria.vehicle(),
                criteria.phone(),
                scopeCompanyId,
                scopeStoreId,
                visibleToSellerUserId
        );
    }

    private void validateTenant(UUID companyId, UUID storeId) {
        if (companyId == null || storeId == null) {
            throw new IllegalArgumentException("companyId and storeId are required");
        }
        companyService.findRequired(companyId);
        Store store = storeService.findRequired(storeId);
        if (!store.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
    }

    private void assertCanAccessLead(Lead lead, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && lead.getCompanyId().equals(requireCompany(authenticatedUser))) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.STORE_MANAGER)
                && lead.getCompanyId().equals(requireCompany(authenticatedUser))
                && lead.getStoreId().equals(requireStore(authenticatedUser))) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.SELLER)
                && lead.getCompanyId().equals(requireCompany(authenticatedUser))
                && lead.getStoreId().equals(requireStore(authenticatedUser))
                && isVisibleToSeller(lead, authenticatedUser.id())) {
            return;
        }
        throw new ForbiddenException("Access denied for lead");
    }

    private void assertCanUseTenant(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(requireCompany(authenticatedUser))) {
            return;
        }
        if (companyId.equals(requireCompany(authenticatedUser)) && storeId.equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for lead tenant");
    }

    private void assertCanAssignUser(UUID userId, UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));
        if (!companyId.equals(user.getCompanyId()) || !storeId.equals(user.getStoreId())) {
            throw new ForbiddenException("Assigned user does not belong to lead store");
        }
        if (hasRole(authenticatedUser, UserRole.ADMIN) || userId.equals(authenticatedUser.id())) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(requireCompany(authenticatedUser))) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.STORE_MANAGER)
                && companyId.equals(requireCompany(authenticatedUser))
                && storeId.equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for assignment");
    }

    private boolean isVisibleToSeller(Lead lead, UUID sellerId) {
        return sellerId.equals(lead.getAssignedToUserId())
                || (lead.getAssignedToUserId() == null && SELLER_AVAILABLE_STATUSES.contains(lead.getStatus()));
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private UUID requireStore(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() == null) {
            throw new ForbiddenException("User is not linked to a store");
        }
        return authenticatedUser.storeId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
