package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tu.kielce.walczak.MusicStore.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Genre findGenreByGenreName(String name);
}
