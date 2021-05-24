package tu.kielce.walczak.MusicStore.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;

import javax.persistence.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "album")
// Podobno jest bug jak się użyje @Date przy mapowaniu many to many itp
@Getter
@Setter
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "albumId")
    private Long albumId;

    @Column(name = "albumTitle")
    private String albumTitle;

    @Column(name = "albumYear")
    private String albumYear;

    @ManyToOne
    @JoinColumn(name = "artistId", nullable = false)
    @JsonManagedReference
    private Artist artist;

    @Column(name = "albumPrice")
    private double albumPrice;

    @ManyToMany(targetEntity = Genre.class, cascade = { CascadeType.ALL })
    @JoinTable(name = "genre_to_album",
            joinColumns = { @JoinColumn(name = "albumId") },
            inverseJoinColumns = { @JoinColumn(name = "genreId") })
    private List<Genre> genres;

    @ManyToMany(targetEntity = Subgenre.class, cascade = { CascadeType.ALL })
    @JoinTable(name = "subgenre_to_album",
            joinColumns = { @JoinColumn(name = "albumId") },
            inverseJoinColumns = { @JoinColumn(name = "subgenreId") })
    private List<Subgenre> subgenres;

    public AbstractMap.SimpleEntry<Long, String> albumToVector() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.artist.getArtistId()).append(';');
        // Max 7 gatunków do albumu - wypełnij ile jest, reszta 0
        int i = 0;
        for (Genre g: genres) {
            builder.append(g.getGenreId()).append(';');
            i++;
        }
        for (; i < 7; i++) {
            builder.append(0).append(';');
        }
        // Max 7 podgatunków do albumu - to samo
        int j = 0;
        for (Subgenre s: subgenres) {
            builder.append(s.getSubgenreId()).append(';');
            j++;
        }
        for (; j < 7; j++) {
            builder.append(0).append(';');
        }
        // Usuń ostatni średnik
        builder.deleteCharAt(builder.length()-1);

        System.out.println(builder.toString());
        return new AbstractMap.SimpleEntry<>(this.albumId, builder.toString());
    }
}
