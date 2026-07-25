package io.plan.mate.expense.tracker.backend.group.jpa.repository;

import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

  @Query(
      "select g from Group g where :search is null "
          + "or lower(g.name) like lower(concat('%', cast(:search as string), '%')) escape '\\' "
          + "or lower(coalesce(g.description, '')) "
          + "like lower(concat('%', cast(:search as string), '%')) escape '\\'")
  Page<Group> search(@Param("search") String search, Pageable pageable);

  @Query(
      "select g from Group g where exists "
          + "(select 1 from Member m where m.group = g and m.user.id = :userId) "
          + "and (:search is null "
          + "or lower(g.name) like lower(concat('%', cast(:search as string), '%')) escape '\\' "
          + "or lower(coalesce(g.description, '')) "
          + "like lower(concat('%', cast(:search as string), '%')) escape '\\')")
  Page<Group> searchForMember(
      @Param("userId") Long userId, @Param("search") String search, Pageable pageable);
}
