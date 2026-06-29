package be.ucll.repository;

import be.ucll.model.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {

    boolean existsByStudentNumber(String studentNumber);
    Student findByStudentNumber(String studentNumber);

    @Query(value = "SELECT COUNT(*) FROM STUDENT_IN_GROUP WHERE UPPER(TRIM(STUDENTNUMBER)) = UPPER(TRIM(:studentNumber))", nativeQuery = true)
    long countInStudentInGroupByStudentNumber(@Param("studentNumber") String studentNumber);

    // Find students assigned to any group that belongs to the given course id (expected attendees)
    List<Student> findDistinctByAssignedGroups_Course_Id(Long courseId);
}
