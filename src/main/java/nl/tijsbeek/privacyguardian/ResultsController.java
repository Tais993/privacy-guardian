package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

/** Displays the overview returned by {@link PrivacyService} after the scanning animation. */
@Controller
public class ResultsController {

    private final PrivacyService privacyService;

    public ResultsController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    @GetMapping("/results")
    public String results(@RequestParam(required = false) String username,
                          @RequestParam(required = false) String urls,
                          Model model) {
        try {
            String overview = username != null && !username.isBlank()
                    ? privacyService.getOverviewByUsername(username.strip())
                    : privacyService.getOverviewByUrls(urlLines(urls));
            model.addAttribute("privacyResult", overview);
        } catch (RuntimeException exception) {
            model.addAttribute("privacyError", "The privacy overview could not be generated. Please check that the required local services are running and try again.");
        }
        return "results";
    }

    private List<String> urlLines(String urls) {
        if (urls == null || urls.isBlank()) {
            return List.of();
        }
        return Arrays.stream(urls.split("\\R"))
                .map(String::strip)
                .filter(url -> !url.isEmpty())
                .toList();
    }
}
