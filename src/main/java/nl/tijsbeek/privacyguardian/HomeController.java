package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;
    private final PrivacyService privacyService;

    public HomeController(HomeService homeService, PrivacyService privacyService) {
        this.homeService = homeService;
        this.privacyService = privacyService;
    }

    @GetMapping("/")
    public String home(Model model) {
        System.out.println(privacyService.getOverviewByUsername("NasiHond"));
        model.addAttribute("welcomeMessage", homeService.getWelcomeMessage());
        return "home";
    }

    @GetMapping("/decision")
    public String decision() {
        return "decision";
    }
}
