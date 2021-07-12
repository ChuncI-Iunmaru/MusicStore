package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;
import tu.kielce.walczak.MusicStore.entity.Genre;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Genre findGenreByGenreName(String name);

}
