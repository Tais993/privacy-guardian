package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        List<String> listOfPrivacyStatements = urls.stream()
                .map(privacyReaderService::lookupPolicy)
                .collect(Collectors.toList());

        return privacySummaryService.generateSummary(listOfPrivacyStatements);
    }
}
