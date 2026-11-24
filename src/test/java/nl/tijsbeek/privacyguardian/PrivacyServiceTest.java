package nl.tijsbeek.privacyguardian;

import nl.tijsbeek.privacyguardian.PrivacyReaderService.PolicyLookup;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivacyServiceTest {

    @Test
    void summarizesReadablePoliciesAsOneBatchAndKeepsFailedLookupsInTheOverview() {
        PolicyLookup bandcamp = new PolicyLookup(URI.create("https://www.bandcamp.com/"),
                URI.create("https://bandcamp.com/privacy"), "Bandcamp policy text", null);
        PolicyLookup github = new PolicyLookup(URI.create("https://github.com/"),
                URI.create("https://docs.github.com/privacy"), null, "Could not read privacy policy");
        PolicyLookup bitbucket = new PolicyLookup(URI.create("https://bitbucket.org/"),
                URI.create("https://www.atlassian.com/legal/privacy-policy"), "Atlassian policy text", null);
        PolicyLookup other = new PolicyLookup(URI.create("https://example.com/"),
                URI.create("https://example.com/privacy"), "Another policy text", null);
        List<String> sites = List.of(bandcamp, github, bitbucket, other).stream()
                .map(policy -> policy.siteUrl().toString())
                .toList();
        Map<URI, PolicyLookup> lookups = Map.of(bandcamp.siteUrl(), bandcamp, github.siteUrl(), github,
                bitbucket.siteUrl(), bitbucket, other.siteUrl(), other);
        SherlockService sherlock = new SherlockService(false, "") {
            @Override
            public List<String> getSiteLinks(String username) {
                return sites;
            }
        };
        PrivacyReaderService reader = new PrivacyReaderService() {
            @Override
            public PolicyLookup lookupPolicy(String url) {
                return lookups.get(URI.create(url));
            }
        };
        HardcodedPrivacySummaryService hardcoded = new HardcodedPrivacySummaryService();
        PrivacyService service = new PrivacyService(sherlock, reader, policies -> {
            assertEquals(List.of(bandcamp, bitbucket, other), policies);
            return hardcoded.generateSummaries(policies);
        });

        String[] entries = service.getOverviewByUsername("test-user").split("\n\n");

        assertEquals(4, entries.length);
        assertTrue(entries[0].startsWith("https://www.bandcamp.com/ (https://bandcamp.com/privacy)\nPredefined summary"));
        assertTrue(entries[0].contains("Bandcamp collects"));
        assertEquals("https://github.com/ (https://docs.github.com/privacy)\nCould not read privacy policy", entries[1]);
        assertTrue(entries[2].contains("Atlassian collects"));
        assertEquals("https://example.com/ (https://example.com/privacy)\nNo predefined summary available", entries[3]);
    }
}
