package be.ucll.repository;

import be.ucll.model.Event;
import be.ucll.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * Find events for a course and fetch their groups to avoid lazy-loading issues
     * when the entities are mapped to DTOs outside of a transactional context.
     */
    @Query("select distinct e from Event e left join fetch e.groups where e.course.id = :id")
    List<Event> findEventsByCourseId(@Param("id") Long id);

    List<Event> findAllByGroupsContains(Group group);
}


