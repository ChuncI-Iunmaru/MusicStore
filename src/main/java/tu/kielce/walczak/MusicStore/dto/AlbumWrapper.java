package tu.kielce.walczak.MusicStore.dto;

import lombok.Data;
import tu.kielce.walczak.MusicStore.entity.Album;

@Data
public class AlbumWrapper {
    private final Album album;
    private final double similarityValue;
}
