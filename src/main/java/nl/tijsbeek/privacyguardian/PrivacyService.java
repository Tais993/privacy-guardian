package nl.tijsbeek.privacyguardian;

import nl.tijsbeek.privacyguardian.PrivacyReaderService.PolicyLookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Builds one privacy policy overview per website.
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

    public List<String> getOverviewByUsername(String username) {
        List<String> siteLinks = sherlockService.getSiteLinks(username);
        return getOverviewByUrls(siteLinks);
    }

    public List<String> getOverviewByUrls(List<String> siteLinks) {
        if (siteLinks.isEmpty()) {
            return List.of();
        }

        List<PolicyLookup> lookups = siteLinks.stream()
                .map(privacyReaderService::lookupPolicy)
                .toList();
        List<PolicyLookup> readablePolicies = lookups.stream()
                .filter(policy -> policy.error() == null)
                .toList();
        Iterator<String> summaries = privacySummaryService.generateSummaries(readablePolicies).iterator();

        List<String> overviews = new ArrayList<>(lookups.size());
        for (int i = 0; i < lookups.size(); i++) {
            String url = siteLinks.get(i);
            PolicyLookup policy = lookups.get(i);
            String heading = policy.policyUrl() == null ? url : url + " (" + policy.policyUrl() + ")";
            String result = policy.error() == null ? summaries.next() : policy.error();
            overviews.add(heading + "\n" + result);
        }

        return List.copyOf(overviews);
    }
}
