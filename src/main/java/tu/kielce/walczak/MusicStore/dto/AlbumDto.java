package tu.kielce.walczak.MusicStore.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlbumDto {
    private final long albumId;
    private final String albumTitle;
    private final String albumYear;
    private final String artistName;
    private final double albumPrice;
    private final String imageUrl;
    private final List<String> genres;
    private final List<String> subgenres;
}
