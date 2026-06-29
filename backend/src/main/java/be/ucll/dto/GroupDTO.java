package be.ucll.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Group entities.
 *
 * Purpose:
 * - Sent to the frontend to represent a Group without exposing JPA internals.
 * - Includes a list of assigned students as StudentDTOs and a reference to the owning course by id.
 *
 * Notes:
 * - Keep the payload small: students are embedded as DTOs but those DTOs do not include back-references to groups.
 */
public class GroupDTO {
    private Long id;
    private String name;
    private Long courseId;
    private List<StudentDTO> assignedStudents = new ArrayList<>();

    /** No-arg constructor needed by Jackson. */
    public GroupDTO() {}

    /**
     * Simple constructor used by mapper when only id/name/course are available.
     */
    public GroupDTO(Long id, String name, Long courseId) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Course id owning this group (nullable). */
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    /** List of students assigned to this group (as StudentDTO). */
    public List<StudentDTO> getAssignedStudents() {
        return assignedStudents;
    }

    public void setAssignedStudents(List<StudentDTO> assignedStudents) {
        this.assignedStudents = assignedStudents;
    }
}
