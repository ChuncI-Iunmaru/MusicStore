package tu.kielce.walczak.MusicStore.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "artist")
@Data
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artistId")
    private long artistId;

    @Column(name = "artistName")
    private String artistName;
}
