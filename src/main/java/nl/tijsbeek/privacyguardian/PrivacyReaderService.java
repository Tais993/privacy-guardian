package nl.tijsbeek.privacyguardian;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PrivacyReaderService {

    private static final Pattern POLICY_LABEL = Pattern.compile("\\bprivacy[\\s_-]+(?:policy|notice|statement)\\b");
    private static final String POLICY_REL = "[rel~=(?i)(^|\\s)privacy-policy(\\s|$)]";
    private static final int MAX_REDIRECTS = 5;

    public String lookupPolicy(String url) {
        URI site = webUri(url);
        if (site == null) {
            return "Invalid HTTP(S) URL";
        }

        try {
            Document page = fetchPage(site);
            Optional<URI> policy = findPolicyLink(page);
            if (policy.isEmpty()) {
                // Use the final page's origin in case the site redirected.
                URI pageUrl = URI.create(page.location());
                URI homepage = pageUrl.resolve("/");
                if (!homepage.equals(pageUrl)) {
                    policy = findPolicyLink(fetchPage(homepage));
                }
            }
            return policy.map(URI::toString).orElse("Privacy policy link not found");
        } catch (IOException | IllegalArgumentException e) {
            return "Could not read website";
        }
    }

    private Document fetchPage(URI uri) throws IOException {
        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            requirePublicHost(uri);
            Connection.Response response = Jsoup.connect(uri.toString())
                    .followRedirects(false)
                    .timeout(5_000)
                    .maxBodySize(2 * 1024 * 1024)
                    .execute();
            if (response.statusCode() < 300) {
                return response.parse();
            }

            String location = response.header("Location");
            if (location == null) {
                throw new IOException("Redirect without a Location header");
            }
            uri = webUri(uri.resolve(location).toString());
            if (uri == null) {
                throw new IOException("Invalid redirect URL");
            }
        }
        throw new IOException("Too many redirects");
    }

    private void requirePublicHost(URI uri) throws IOException {
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            byte[] octets = address.getAddress();
            boolean uniqueLocal = octets.length == 16 && (octets[0] & 0xfe) == 0xfc;
            boolean sharedAddress = octets.length == 4 && octets[0] == 100 && (octets[1] & 0xc0) == 0x40;
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress() || uniqueLocal || sharedAddress) {
                throw new IOException("Website resolves to a non-public address");
            }
        }
    }

    private Optional<URI> findPolicyLink(Document page) {
        URI bestLink = null;
        int bestScore = 0;
        for (Element link : page.select("a[href], link" + POLICY_REL + "[href]")) {
            String href = link.attr("href").strip();
            if (href.isEmpty() || href.startsWith("#")) {
                continue;
            }
            URI target = webUri(link.absUrl("href"));
            if (target == null) {
                continue;
            }

            int score = policyScore(link, target);
            if (score > bestScore) {
                bestLink = target;
                bestScore = score;
            }
        }
        return Optional.ofNullable(bestLink);
    }

    private int policyScore(Element link, URI target) {
        String label = link.text().strip().toLowerCase(Locale.ROOT);
        String path = target.getPath().toLowerCase(Locale.ROOT).replaceAll("/+$", "");
        if (link.is(POLICY_REL)) {
            return 4;
        }
        if (POLICY_LABEL.matcher(label).find()) {
            return 3;
        }
        if (POLICY_LABEL.matcher(path).find()) {
            return 2;
        }
        return label.equals("privacy") || path.endsWith("/privacy") ? 1 : 0;
    }

    private URI webUri(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = URI.create(value.strip());
            if (uri.getHost() != null && uri.getUserInfo() == null
                    && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
                return uri;
            }
        } catch (IllegalArgumentException ignored) {
            // Malformed input and malformed page links are both unusable.
        }
        return null;
    }
}
