package nl.tijsbeek.privacyguardian;

import nl.tijsbeek.privacyguardian.PrivacyReaderService.PolicyLookup;

import java.util.List;

public interface PrivacySummaryService {

    /** Returns one result per successfully read policy, in the same order as the input. */
    List<String> generateSummaries(List<PolicyLookup> policies);
}
