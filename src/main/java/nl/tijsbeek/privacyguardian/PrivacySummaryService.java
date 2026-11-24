package nl.tijsbeek.privacyguardian;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Generates concise privacy-statement summaries with the locally running Qwen model. */
@Service
public class PrivacySummaryService {

    private final RestClient ollamaClient;

    public PrivacySummaryService(@Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl) {
        this.ollamaClient = RestClient.create(ollamaBaseUrl);
    }

    public String generateSummary(List<String> privacyStatements) {
        String statements = privacyStatements.stream()
                .filter(statement -> statement != null && !statement.isBlank())
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));

        if (statements.isBlank()) {
            return "No privacy statements were found to summarize.";
        }

        OllamaChatResponse response = ollamaClient.post()
                .uri("/api/chat")
                .body(new OllamaChatRequest(
                        "qwen3:4b",
                        false,
                        List.of(
                                new OllamaMessage("system", "You summarize privacy statements. Produce a concise, factual overview of data collection, purposes, sharing, retention, user rights, and risks. Do not invent facts; clearly identify missing or unclear information. Write the summary in a clear and easy-to-understand manner."),
                                new OllamaMessage("user", "Summarize these privacy statements:\n\n" + statements)
                        )
                ))
                .retrieve()
                .body(OllamaChatResponse.class);

        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Ollama returned no summary.");
        }
        return response.message().content();
    }

    private record OllamaChatRequest(String model, boolean stream, List<OllamaMessage> messages) { }

    private record OllamaMessage(String role, String content) { }

    private record OllamaChatResponse(OllamaMessage message) { }
}
