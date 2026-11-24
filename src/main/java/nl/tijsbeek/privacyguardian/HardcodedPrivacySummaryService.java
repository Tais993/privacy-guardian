package nl.tijsbeek.privacyguardian;

import nl.tijsbeek.privacyguardian.PrivacyReaderService.PolicyLookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "false", matchIfMissing = true)
public class HardcodedPrivacySummaryService implements PrivacySummaryService {

    private static final Map<String, String> SUMMARIES_BY_HOST = Map.ofEntries(
            Map.entry("7cups.com", """
                    - 7 Cups collects personal information to provide online mental health and emotional support services.
                    - Private messages linked to an identifiable account can be stored for up to a year.
                    - 7 Cups may anonymize and aggregate data, including chat transcripts, before deletion.
                    """),
            Map.entry("bandcamp.com", """
                    - Bandcamp collects profile details and purchase history. A payment processor handles card details.
                    - Bandcamp may show parts of a fan profile or purchase history to other users.
                    - Fans can make individual purchases private.
                    """),
            Map.entry("bitbucket.org", """
                    - Atlassian collects account, device, and usage information to operate, secure, and improve its services.
                    - Atlassian uses service providers for functions such as hosting, storage, and support.
                    - You can request a copy of basic account information and content in spaces you control, such as a personal Bitbucket repository.
                    """),
            Map.entry("bsky.app", """
                    - Bluesky collects account details and activity data such as posts viewed and links clicked.
                    - Posts and comments are public.
                    - Bluesky stores unencrypted direct messages and can access them for trust and safety purposes.
                    """),
            Map.entry("chess.com", """
                    - Chess.com collects account identifiers and gameplay information such as ratings and club membership.
                    - Usage data can include IP address, device identifiers, and pages visited.
                    - Rights to access, correct, or delete personal information depend on where you live.
                    """),
            Map.entry("dev.to", """
                    - DEV Community collects names, email addresses, and information shared through posts and other interactive features.
                    - Public profile information and followed topics can be visible to anyone, including recruiters.
                    - Third-party payment processors handle shop payments.
                    """),
            Map.entry("github.com", """
                    - GitHub collects account details, content you submit, and usage data such as IP address and device information.
                    - Analytics and advertising cookies may be used depending on your jurisdiction and choices.
                    - An employer or school that provides your GitHub account can manage it and access account data and content.
                    """));

    @Override
    public List<String> generateSummaries(List<PolicyLookup> policies) {
        return policies.stream().map(this::predefinedSummary).toList();
    }

    private String predefinedSummary(PolicyLookup policy) {
        String host = policy.siteUrl().getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        if (host.equals("bsky.social")) {
            host = "bsky.app";
        }

        String summary = SUMMARIES_BY_HOST.get(host);
        return summary == null ? "No predefined summary available" : "Predefined summary\n" + summary.strip();
    }
}
