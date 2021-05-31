package tu.kielce.walczak.MusicStore.service;

import tu.kielce.walczak.MusicStore.dto.UserPreferences;

public interface UserService {
    UserPreferences getUserPreferencesByEmail(String email);
}
