package com.eai.application.email;

import com.eai.application.lead.LeadRepository;
import com.eai.domain.lead.Lead;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DuplicateLeadCheckerTest {

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final DuplicateLeadChecker checker = new DuplicateLeadChecker(leadRepository);

    @DisplayName("Duplicidade de e-mail usa telefone normalizado e loja")
    @Test
    void findsDuplicateByNormalizedPhoneAndStore() {
        Lead lead = mock(Lead.class);
        when(leadRepository.findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999998888")))
                .thenReturn(Optional.of(lead));

        Optional<Lead> duplicate = checker.findPossibleDuplicate(STORE_ID, "(11) 99999-8888");

        assertThat(duplicate).contains(lead);
        verify(leadRepository).findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999998888"));
    }

    @DisplayName("Veiculo nao participa da regra de duplicidade por e-mail")
    @Test
    void ignoresVehicleInterestWhenCheckingDuplicate() {
        when(leadRepository.findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999998888")))
                .thenReturn(Optional.empty());

        boolean duplicate = checker.isPossibleDuplicate(STORE_ID, "(11) 99999-8888", "Honda Civic");

        assertThat(duplicate).isFalse();
        verify(leadRepository).findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999998888"));
    }
}
