package be.ucll.repository;

import be.ucll.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Group findByName(String name);
    boolean existsByName(String name);

    List<Group> findGroupsByCourse_Id(Long courseId);

    Group findByCourseIdAndName(Long id, String groupName);
}
