package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tu.kielce.walczak.MusicStore.entity.Artist;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
