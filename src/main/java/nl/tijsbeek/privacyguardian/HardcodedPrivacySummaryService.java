package nl.tijsbeek.privacyguardian;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "false", matchIfMissing = true)
public class HardcodedPrivacySummaryService implements PrivacySummaryService {

    private static final String SAMPLE_SUMMARY = """
            Sample summary for testing (Ollama is disabled).
            - Collection: Example account and usage data.
            - Purposes: Example service operation and analytics.
            - Sharing: Example service providers.
            - Retention: Example retention period unspecified.
            - User rights: Example access and deletion requests.
            - Privacy risks: Example third-party processing.
            """.strip();

    @Override
    public String generateSummary(String policyText) {
        return SAMPLE_SUMMARY;
    }
}
