package bugBust.transitgo.repository;

import bugBust.transitgo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    void deleteById(Long id);



    Optional<User> findByBusid(String busId);


    boolean existsByBusid(String busId);

    @Query( "SELECT u FROM User u")
   List<User> findAll();
}

