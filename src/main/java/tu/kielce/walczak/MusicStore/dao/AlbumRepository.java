package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tu.kielce.walczak.MusicStore.entity.Album;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
