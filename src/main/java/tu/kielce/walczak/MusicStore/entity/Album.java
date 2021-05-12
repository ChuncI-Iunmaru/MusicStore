package tu.kielce.walczak.MusicStore.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
}
