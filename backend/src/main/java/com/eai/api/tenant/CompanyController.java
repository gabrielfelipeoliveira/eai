package com.eai.api.tenant;

import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.CreateCompanyCommand;
import com.eai.application.tenant.UpdateCompanyCommand;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyResponse> listCompanies() {
        return companyService.listCompanies().stream()
                .map(CompanyResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    public CompanyResponse getCompany(@PathVariable UUID id) {
        return CompanyResponse.fromDomain(companyService.getCompany(id));
    }

    @PostMapping
    public CompanyResponse createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return CompanyResponse.fromDomain(companyService.createCompany(new CreateCompanyCommand(
                request.name()
        )));
    }

    @PutMapping("/{id}")
    public CompanyResponse updateCompany(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        return CompanyResponse.fromDomain(companyService.updateCompany(id, new UpdateCompanyCommand(
                request.name(),
                request.status()
        )));
    }
}
