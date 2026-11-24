package nl.tijsbeek.privacyguardian;

import nl.tijsbeek.privacyguardian.PrivacyReaderService.PolicyLookup;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

/**
 * Combines service results into an overview of privacy policies for a username.
 */
@Service
public class PrivacyService {

    private final SherlockService sherlockService;
    private final PrivacyReaderService privacyReaderService;
    private final PrivacySummaryService privacySummaryService;

    public PrivacyService(SherlockService sherlockService,
                          PrivacyReaderService privacyReaderService,
                          PrivacySummaryService privacySummaryService) {
        this.sherlockService = sherlockService;
        this.privacyReaderService = privacyReaderService;
        this.privacySummaryService = privacySummaryService;
    }

    public String getOverviewByUsername(String username) {
        List<String> siteLinks = sherlockService.getSiteLinks(username);
        if (siteLinks.isEmpty()) {
            return "No websites were found to check for privacy policies.";
        }

        List<PolicyLookup> lookups = siteLinks.stream()
                .map(privacyReaderService::lookupPolicy)
                .toList();
        List<PolicyLookup> readablePolicies = lookups.stream()
                .filter(policy -> policy.error() == null)
                .toList();
        Iterator<String> summaries = privacySummaryService.generateSummaries(readablePolicies).iterator();

        StringJoiner overview = new StringJoiner("\n\n");
        for (int i = 0; i < lookups.size(); i++) {
            String url = siteLinks.get(i);
            PolicyLookup policy = lookups.get(i);
            String heading = policy.policyUrl() == null ? url : url + " (" + policy.policyUrl() + ")";
            String result = policy.error() == null ? summaries.next() : policy.error();
            overview.add(heading + "\n" + result);
        }

        return overview.toString();
    }
}
