package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import tu.kielce.walczak.MusicStore.entity.Artist;

@CrossOrigin("http://localhost:4200")
public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
