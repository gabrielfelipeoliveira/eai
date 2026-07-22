package com.eai.application.tenant;

import com.eai.application.common.NotFoundException;
import com.eai.application.user.UserRepository;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.TenantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Company> listCompanies() {
        return companyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Company getCompany(UUID id) {
        return findRequired(id);
    }

    @Transactional
    public Company createCompany(CreateCompanyCommand command) {
        return companyRepository.save(Company.create(command.name()));
    }

    @Transactional
    public Company updateCompany(UUID id, UpdateCompanyCommand command) {
        Company company = findRequired(id);
        if (company.getStatus() != TenantStatus.INACTIVE
                && command.status() == TenantStatus.INACTIVE
                && userRepository.existsActiveByCompanyId(id)) {
            throw new IllegalArgumentException("Cannot deactivate company with active users");
        }
        company.update(command.name(), command.status());
        return companyRepository.save(company);
    }

    public Company findRequired(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

}
