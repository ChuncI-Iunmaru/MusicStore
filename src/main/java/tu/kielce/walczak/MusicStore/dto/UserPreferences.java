package tu.kielce.walczak.MusicStore.dto;

import lombok.Data;
import tu.kielce.walczak.MusicStore.entity.Artist;
import tu.kielce.walczak.MusicStore.entity.Genre;
import tu.kielce.walczak.MusicStore.entity.Subgenre;

import java.util.List;
import java.util.Map;

@Data
public class UserPreferences {
    private final Map<String, Double> weightedGenres;
    private final Map<String, Double> weightedSubgenres;
    private final Map<String, Double> weightedArtists;
    private final Map<String, Double> years;
    private final Map<String, Double> prices;
    private final long totalItems;
}
