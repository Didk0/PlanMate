package io.plan.mate.expense.tracker.backend.user.jpa.repository;

import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByKeycloakId(UUID keycloakId);

  Optional<User> findByUsername(String name);
}
