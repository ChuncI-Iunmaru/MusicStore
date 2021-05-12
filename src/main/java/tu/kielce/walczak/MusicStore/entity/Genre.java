package tu.kielce.walczak.MusicStore.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "genre")
@Data
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genreId")
    private Long genreId;

    @Column(name = "genreName")
    private String genreName;
}
