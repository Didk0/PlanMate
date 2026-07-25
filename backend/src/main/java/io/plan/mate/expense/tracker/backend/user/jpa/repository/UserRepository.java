package io.plan.mate.expense.tracker.backend.user.jpa.repository;

import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByKeycloakId(UUID keycloakId);

  Optional<User> findByUsername(String name);

  List<User> findByUsernameIn(Collection<String> usernames);

  @Query(
      "select u from User u where :search is null "
          + "or lower(u.username) like lower(concat('%', cast(:search as string), '%'))")
  Page<User> search(@Param("search") String search, Pageable pageable);
}
