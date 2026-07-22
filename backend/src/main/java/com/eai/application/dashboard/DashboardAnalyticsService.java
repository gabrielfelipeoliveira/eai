package com.eai.application.dashboard;

import com.eai.application.common.ForbiddenException;
import com.eai.application.distribution.LeadSlaEvaluator;
import com.eai.application.distribution.LeadSlaPolicyRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private static final int DEFAULT_MINUTES_TO_ASSIGN = 15;
    private static final int DEFAULT_MINUTES_TO_FIRST_CONTACT = 30;

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadSlaPolicyRepository slaPolicyRepository;
    private final LeadSlaEvaluator slaEvaluator = new LeadSlaEvaluator();

    @Transactional(readOnly = true)
    public DashboardSummary summary(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        DashboardScope scope = resolveScope(filters, authenticatedUser);
        List<Lead> periodLeads = findLeads(scope, filters.dateFrom(), filters.dateTo());
        Instant now = Instant.now();
        Instant todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant tomorrowStart = todayStart.plus(Duration.ofDays(1));
        Instant monthStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        long soldLeads = countStatus(periodLeads, LeadStatus.SOLD);
        long lostLeads = countStatus(periodLeads, LeadStatus.LOST);
        long closedLeads = soldLeads + lostLeads;

        return new DashboardSummary(
                findLeads(scope, todayStart, tomorrowStart).size(),
                findLeads(scope, monthStart, null).size(),
                countStatus(periodLeads, LeadStatus.AVAILABLE),
                periodLeads.stream().filter(lead -> lead.getAssignedToUserId() != null).count(),
                soldLeads,
                lostLeads,
                closedLeads == 0 ? 0 : percent(soldLeads, closedLeads),
                averageFirstResponseMinutes(periodLeads),
                countOverdue(periodLeads, now)
        );
    }

    @Transactional(readOnly = true)
    public List<DashboardChartItem> leadsBySource(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        List<Lead> leads = findLeads(resolveScope(filters, authenticatedUser), filters.dateFrom(), filters.dateTo());
        Map<LeadSource, Long> counts = leads.stream()
                .collect(Collectors.groupingBy(Lead::getSource, () -> new EnumMap<>(LeadSource.class), Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> new DashboardChartItem(entry.getKey().name(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardChartItem::label))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardChartItem> leadsByStatus(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        List<Lead> leads = findLeads(resolveScope(filters, authenticatedUser), filters.dateFrom(), filters.dateTo());
        Map<LeadStatus, Long> counts = leads.stream()
                .collect(Collectors.groupingBy(Lead::getStatus, () -> new EnumMap<>(LeadStatus.class), Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> new DashboardChartItem(entry.getKey().name(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardChartItem::label))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardSellerItem> leadsBySeller(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        List<Lead> leads = findLeads(resolveScope(filters, authenticatedUser), filters.dateFrom(), filters.dateTo()).stream()
                .filter(lead -> lead.getAssignedToUserId() != null)
                .toList();
        Map<UUID, User> users = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return leads.stream()
                .collect(Collectors.groupingBy(Lead::getAssignedToUserId))
                .entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().size();
                    long sold = countStatus(entry.getValue(), LeadStatus.SOLD);
                    User user = users.get(entry.getKey());
                    return new DashboardSellerItem(
                            entry.getKey(),
                            user == null ? "Vendedor sem cadastro" : user.getName(),
                            total,
                            sold,
                            total == 0 ? 0 : percent(sold, total)
                    );
                })
                .sorted(Comparator.comparing(DashboardSellerItem::soldLeads).thenComparing(DashboardSellerItem::leadCount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardSalesPeriodItem> salesByPeriod(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        return findLeads(resolveScope(filters, authenticatedUser), filters.dateFrom(), filters.dateTo()).stream()
                .filter(lead -> lead.getStatus() == LeadStatus.SOLD)
                .collect(Collectors.groupingBy(this::salesPeriod))
                .entrySet().stream()
                .map(entry -> new DashboardSalesPeriodItem(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(Lead::getSaleValue)
                                .filter(value -> value != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparing(DashboardSalesPeriodItem::period))
                .toList();
    }

    private List<Lead> findLeads(DashboardScope scope, Instant dateFrom, Instant dateTo) {
        return leadRepository.findAll(new LeadSearchCriteria(
                null,
                null,
                scope.assignedToUserId(),
                scope.storeId(),
                dateFrom,
                dateTo,
                null,
                null,
                null,
                scope.companyId(),
                scope.storeScopeId()
        ));
    }

    private DashboardScope resolveScope(DashboardFilters filters, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return new DashboardScope(filters.companyId(), filters.storeId(), null);
        }
        UUID companyId = requireCompany(authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.SELLER)) {
            return new DashboardScope(companyId, requireStore(authenticatedUser), authenticatedUser.id());
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER)) {
            UUID storeId = authenticatedUser.storeId() == null ? filters.storeId() : authenticatedUser.storeId();
            return new DashboardScope(companyId, storeId, null);
        }
        throw new ForbiddenException("Access denied for dashboard");
    }

    private long countStatus(List<Lead> leads, LeadStatus status) {
        return leads.stream().filter(lead -> lead.getStatus() == status).count();
    }

    private double averageFirstResponseMinutes(List<Lead> leads) {
        return leads.stream()
                .filter(lead -> lead.getAssignedAt() != null && lead.getFirstContactAt() != null)
                .mapToLong(lead -> Duration.between(lead.getAssignedAt(), lead.getFirstContactAt()).toMinutes())
                .average()
                .orElse(0);
    }

    private long countOverdue(List<Lead> leads, Instant now) {
        return leads.stream()
                .filter(lead -> {
                    LeadSlaPolicy policy = findOrDefaultSla(lead.getCompanyId(), lead.getStoreId());
                    return slaEvaluator.isOverdueToAssign(lead, policy, now) || slaEvaluator.isOverdueToFirstContact(lead, policy, now);
                })
                .count();
    }

    private LeadSlaPolicy findOrDefaultSla(UUID companyId, UUID storeId) {
        return slaPolicyRepository.findByCompanyIdAndStoreId(companyId, storeId)
                .orElseGet(() -> LeadSlaPolicy.create(companyId, storeId, DEFAULT_MINUTES_TO_ASSIGN, DEFAULT_MINUTES_TO_FIRST_CONTACT, false));
    }

    private String salesPeriod(Lead lead) {
        Instant date = lead.getLastContactAt() == null ? lead.getUpdatedAt() : lead.getLastContactAt();
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(date);
    }

    private double percent(long value, long total) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .doubleValue();
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

    private record DashboardScope(UUID companyId, UUID storeId, UUID assignedToUserId) {
        private UUID storeScopeId() {
            return storeId;
        }
    }
}
