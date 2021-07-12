package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tu.kielce.walczak.MusicStore.entity.Subgenre;

public interface SubgenreRepository extends JpaRepository<Subgenre, Long> {
    Subgenre findSubgenreBySubgenreName(String name);
}
