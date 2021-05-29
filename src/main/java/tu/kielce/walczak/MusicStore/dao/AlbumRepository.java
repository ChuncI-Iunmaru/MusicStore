package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import tu.kielce.walczak.MusicStore.entity.Album;

@RepositoryRestResource
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findByAlbumTitleContaining(@RequestParam("title") String title, Pageable pageable);
}
