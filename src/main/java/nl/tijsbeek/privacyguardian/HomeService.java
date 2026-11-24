package nl.tijsbeek.privacyguardian;

import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getWelcomeMessage() {
        return "Welcome to Privacy Guardian.";
    }
}
