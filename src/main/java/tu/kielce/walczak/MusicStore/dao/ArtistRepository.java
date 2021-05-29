package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tu.kielce.walczak.MusicStore.entity.Artist;

@RepositoryRestResource
public interface ArtistRepository extends JpaRepository<Artist, Long> {
}