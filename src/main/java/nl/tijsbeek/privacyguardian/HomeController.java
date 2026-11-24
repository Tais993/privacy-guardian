package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("welcomeMessage", homeService.getWelcomeMessage());
        return "home";
    }

    @GetMapping("/decision")
    public String decision() {
        return "decision";
    }
}
