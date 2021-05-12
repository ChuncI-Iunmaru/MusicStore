package tu.kielce.walczak.MusicStore.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "album")
@Data
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "albumId")
    private long albumId;

    @Column(name = "albumTitle")
    private String albumTitle;

    @Column(name = "albumYear")
    private String albumYear;

    @OneToOne
    @JoinColumn(name = "artistId", referencedColumnName = "artistId")
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
