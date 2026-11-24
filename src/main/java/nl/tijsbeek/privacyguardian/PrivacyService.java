package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.StringJoiner;

/**
 * Combines service results into an overview of privacy policies by username or URL.
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
        return getOverviewByURLs(siteLinks);
    }

    public String getOverviewByURLs(List<String> urls) {
        StringJoiner overview = new StringJoiner("\n\n");
        overview.setEmptyValue("No websites were found to check for privacy policies.");
        for (String url : urls) {
            PrivacyReaderService.PolicyLookup policy = privacyReaderService.lookupPolicy(url);
            String heading = policy.policyUrl() == null ? url : url + " (" + policy.policyUrl() + ")";
            String result = policy.error();
            if (result == null) {
                try {
                    result = privacySummaryService.generateSummary(policy.text());
                } catch (RestClientException | IllegalStateException e) {
                    result = "Could not summarize privacy policy with Ollama";
                }
            }
            overview.add(heading + "\n" + result);
        }

        return overview.toString();
    }
}
