package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tu.kielce.walczak.MusicStore.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
