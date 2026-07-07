package com.eai.application.tenant;

import com.eai.application.common.ConflictException;
import com.eai.application.common.NotFoundException;
import com.eai.domain.tenant.Company;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

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
        String document = normalizeDocument(command.document());
        if (companyRepository.existsByDocument(document)) {
            throw new ConflictException("Company document already registered");
        }
        return companyRepository.save(Company.create(command.name(), document, command.email(), command.phone()));
    }

    @Transactional
    public Company updateCompany(UUID id, UpdateCompanyCommand command) {
        Company company = findRequired(id);
        String document = normalizeDocument(command.document());
        if (companyRepository.existsByDocumentAndIdNot(document, id)) {
            throw new ConflictException("Company document already registered");
        }
        company.update(command.name(), document, command.email(), command.phone(), command.status());
        return companyRepository.save(company);
    }

    public Company findRequired(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    private String normalizeDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("document is required");
        }
        return document.trim();
    }
}
