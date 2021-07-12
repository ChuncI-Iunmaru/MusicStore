package tu.kielce.walczak.MusicStore.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlbumDto {
    private final long id;
    private final String title;
    private final String year;
    private final String artistName;
    private final double price;
    private final String imageUrl;
    private final List<String> genres;
    private final List<String> subgenres;
}
