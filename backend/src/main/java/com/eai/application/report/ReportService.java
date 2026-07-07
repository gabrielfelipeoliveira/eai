package com.eai.application.report;

import com.eai.application.common.ForbiddenException;
import com.eai.application.distribution.LeadSlaEvaluator;
import com.eai.application.distribution.LeadSlaPolicyRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int DEFAULT_MINUTES_TO_ASSIGN = 15;
    private static final int DEFAULT_MINUTES_TO_FIRST_CONTACT = 30;

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadSlaPolicyRepository slaPolicyRepository;
    private final List<ReportExporter> exporters;
    private final LeadSlaEvaluator slaEvaluator = new LeadSlaEvaluator();

    public ReportService(
            LeadRepository leadRepository,
            UserRepository userRepository,
            LeadSlaPolicyRepository slaPolicyRepository,
            List<ReportExporter> exporters
    ) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.exporters = exporters;
    }

    @Transactional(readOnly = true)
    public List<ReportLeadPeriodItem> leadsByPeriod(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        return findLeads(filters, authenticatedUser).stream()
                .collect(Collectors.groupingBy(this::createdPeriod))
                .entrySet().stream()
                .map(entry -> {
                    List<Lead> leads = entry.getValue();
                    long sold = countStatus(leads, LeadStatus.SOLD);
                    long lost = countStatus(leads, LeadStatus.LOST);
                    return new ReportLeadPeriodItem(entry.getKey(), leads.size(), sold, lost, conversionRate(sold, sold + lost));
                })
                .sorted(Comparator.comparing(ReportLeadPeriodItem::period))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportSellerItem> leadsBySeller(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        Map<UUID, User> users = usersById();
        return findLeads(filters, authenticatedUser).stream()
                .filter(lead -> lead.getAssignedToUserId() != null)
                .collect(Collectors.groupingBy(Lead::getAssignedToUserId))
                .entrySet().stream()
                .map(entry -> toSellerItem(entry.getKey(), entry.getValue(), users))
                .sorted(Comparator.comparing(ReportSellerItem::soldLeads).thenComparing(ReportSellerItem::leadCount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportSourceItem> leadsBySource(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        return findLeads(filters, authenticatedUser).stream()
                .collect(Collectors.groupingBy(lead -> lead.getSource().name()))
                .entrySet().stream()
                .map(entry -> {
                    List<Lead> leads = entry.getValue();
                    long sold = countStatus(leads, LeadStatus.SOLD);
                    long lost = countStatus(leads, LeadStatus.LOST);
                    return new ReportSourceItem(entry.getKey(), leads.size(), sold, lost, conversionRate(sold, sold + lost));
                })
                .sorted(Comparator.comparing(ReportSourceItem::source))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportLostLeadItem> lostLeads(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        Map<UUID, User> users = usersById();
        return findLeads(filters, authenticatedUser).stream()
                .filter(lead -> lead.getStatus() == LeadStatus.LOST)
                .map(lead -> new ReportLostLeadItem(
                        lead.getId(),
                        lead.getCustomerName(),
                        lead.getVehicleInterest(),
                        lead.getAssignedToUserId(),
                        sellerName(lead.getAssignedToUserId(), users),
                        lead.getSource().name(),
                        lead.getLostReason(),
                        lead.getCreatedAt(),
                        closedAt(lead)
                ))
                .sorted(Comparator.comparing(ReportLostLeadItem::lostAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportSaleItem> sales(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        Map<UUID, User> users = usersById();
        return findLeads(filters, authenticatedUser).stream()
                .filter(lead -> lead.getStatus() == LeadStatus.SOLD)
                .map(lead -> new ReportSaleItem(
                        lead.getId(),
                        lead.getCustomerName(),
                        lead.getVehicleInterest(),
                        lead.getAssignedToUserId(),
                        sellerName(lead.getAssignedToUserId(), users),
                        lead.getSource().name(),
                        lead.getSaleValue(),
                        lead.getCreatedAt(),
                        closedAt(lead)
                ))
                .sorted(Comparator.comparing(ReportSaleItem::soldAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportSlaSummary sla(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        List<Lead> leads = findLeads(filters, authenticatedUser);
        Instant now = Instant.now();
        long overdueToAssign = 0;
        long overdueToFirstContact = 0;
        long withinSla = 0;
        long outsideSla = 0;

        for (Lead lead : leads) {
            LeadSlaPolicy policy = findOrDefaultSla(lead.getCompanyId(), lead.getStoreId());
            boolean assignLate = slaEvaluator.isOverdueToAssign(lead, policy, now);
            boolean contactLate = slaEvaluator.isOverdueToFirstContact(lead, policy, now);
            if (assignLate) {
                overdueToAssign++;
            }
            if (contactLate) {
                overdueToFirstContact++;
            }
            if (lead.getAssignedAt() != null && lead.getFirstContactAt() != null) {
                long minutes = Duration.between(lead.getAssignedAt(), lead.getFirstContactAt()).toMinutes();
                if (policy.isActive() && minutes > policy.getMinutesToFirstContact()) {
                    outsideSla++;
                } else {
                    withinSla++;
                }
            }
        }

        return new ReportSlaSummary(
                leads.size(),
                overdueToAssign,
                overdueToFirstContact,
                overdueToAssign + overdueToFirstContact,
                averageFirstResponseMinutes(leads),
                withinSla,
                outsideSla
        );
    }

    @Transactional(readOnly = true)
    public ExportedReport exportLeadPeriodsCsv(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        return exporter(ReportExportFormat.CSV).exportLeadPeriods(leadsByPeriod(filters, authenticatedUser));
    }

    @Transactional(readOnly = true)
    public ExportedReport exportSellersCsv(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        return exporter(ReportExportFormat.CSV).exportSellers(leadsBySeller(filters, authenticatedUser));
    }

    private List<Lead> findLeads(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        ReportScope scope = resolveScope(filters, authenticatedUser);
        return leadRepository.findAll(new LeadSearchCriteria(
                null,
                filters.source(),
                scope.sellerId(),
                scope.storeId(),
                filters.dateFrom(),
                filters.dateTo(),
                null,
                null,
                null,
                scope.companyId(),
                scope.storeScopeId()
        ));
    }

    private ReportScope resolveScope(ReportFilters filters, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return new ReportScope(filters.companyId(), filters.storeId(), filters.sellerId());
        }

        UUID companyId = requireCompany(authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.SELLER)) {
            return new ReportScope(companyId, requireStore(authenticatedUser), authenticatedUser.id());
        }

        if (hasRole(authenticatedUser, UserRole.MANAGER) || hasRole(authenticatedUser, UserRole.AUDITOR)) {
            UUID storeId = authenticatedUser.storeId() == null ? filters.storeId() : authenticatedUser.storeId();
            return new ReportScope(companyId, storeId, filters.sellerId());
        }

        throw new ForbiddenException("Access denied for reports");
    }

    private ReportSellerItem toSellerItem(UUID sellerId, List<Lead> leads, Map<UUID, User> users) {
        long sold = countStatus(leads, LeadStatus.SOLD);
        long lost = countStatus(leads, LeadStatus.LOST);
        return new ReportSellerItem(
                sellerId,
                sellerName(sellerId, users),
                leads.size(),
                sold,
                lost,
                conversionRate(sold, sold + lost),
                averageFirstResponseMinutes(leads),
                saleValue(leads)
        );
    }

    private Map<UUID, User> usersById() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private String sellerName(UUID sellerId, Map<UUID, User> users) {
        if (sellerId == null) {
            return "Sem vendedor";
        }
        User user = users.get(sellerId);
        return user == null ? "Vendedor sem cadastro" : user.getName();
    }

    private BigDecimal saleValue(List<Lead> leads) {
        return leads.stream()
                .map(Lead::getSaleValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double averageFirstResponseMinutes(List<Lead> leads) {
        return leads.stream()
                .filter(lead -> lead.getAssignedAt() != null && lead.getFirstContactAt() != null)
                .mapToLong(lead -> Duration.between(lead.getAssignedAt(), lead.getFirstContactAt()).toMinutes())
                .average()
                .orElse(0);
    }

    private long countStatus(List<Lead> leads, LeadStatus status) {
        return leads.stream().filter(lead -> lead.getStatus() == status).count();
    }

    private String createdPeriod(Lead lead) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(lead.getCreatedAt());
    }

    private Instant closedAt(Lead lead) {
        return lead.getLastContactAt() == null ? lead.getUpdatedAt() : lead.getLastContactAt();
    }

    private double conversionRate(long sold, long closed) {
        if (closed == 0) {
            return 0;
        }
        return BigDecimal.valueOf(sold)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(closed), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private LeadSlaPolicy findOrDefaultSla(UUID companyId, UUID storeId) {
        return slaPolicyRepository.findByCompanyIdAndStoreId(companyId, storeId)
                .orElseGet(() -> LeadSlaPolicy.create(companyId, storeId, DEFAULT_MINUTES_TO_ASSIGN, DEFAULT_MINUTES_TO_FIRST_CONTACT, false));
    }

    private ReportExporter exporter(ReportExportFormat format) {
        return exporters.stream()
                .filter(exporter -> exporter.supports(format))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Report exporter not configured: " + format));
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

    private record ReportScope(UUID companyId, UUID storeId, UUID sellerId) {
        private UUID storeScopeId() {
            return storeId;
        }
    }
}
