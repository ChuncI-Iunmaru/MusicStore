package tu.kielce.walczak.MusicStore.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "subgenre")
@Data
public class Subgenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subgenreId")
    private Long subgenreId;

    @Column(name = "subgenreName")
    private String subgenreName;
}
