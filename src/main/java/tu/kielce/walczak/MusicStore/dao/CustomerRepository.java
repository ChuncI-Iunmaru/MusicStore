package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tu.kielce.walczak.MusicStore.dto.CustomerIdOnly;
import tu.kielce.walczak.MusicStore.entity.Customer;

@RepositoryRestResource
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);
    CustomerIdOnly findFirstByEmail(String email);
}
