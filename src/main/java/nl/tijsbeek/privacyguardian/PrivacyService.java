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

    public PrivacyService(SherlockService sherlockService, PrivacyReaderService privacyReaderService) {
        this.sherlockService = sherlockService;
        this.privacyReaderService = privacyReaderService;
    }

    public String getOverviewByUsername(String username) {
        List<String> siteLinks = sherlockService.getSiteLinks(username);
        return getOverviewByURLs(siteLinks);
    }

    public String getOverviewByURLs(List<String> urls) {
        return urls.stream()
                .map(url -> url + " -> " + privacyReaderService.lookupPolicy(url))
                .collect(Collectors.joining("\n"));
    }
}
