package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tu.kielce.walczak.MusicStore.dto.UserPreferences;
import tu.kielce.walczak.MusicStore.service.UserService;

@RestController
@RequestMapping("/api/prefs")
public class UserPreferencesController {
    private UserService userService;

    @Autowired
    public UserPreferencesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/byEmail")
    public UserPreferences getPreferencesByEmail(@RequestParam("email") String email) {
        return userService.getUserPreferencesByEmail(email);
    }
}
