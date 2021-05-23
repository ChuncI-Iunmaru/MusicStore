package tu.kielce.walczak.MusicStore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "artist")
@Getter
@Setter
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artistId")
    private Long artistId;

    @Column(name = "artistName")
    private String artistName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "artist")
    @JsonBackReference
    private Set<Album> albums = new HashSet<>();
}
