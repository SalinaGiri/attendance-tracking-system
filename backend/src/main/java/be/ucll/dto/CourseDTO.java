package be.ucll.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Course entities.
 *
 * Purpose:
 * - Provide a stable, minimal JSON representation of Course for the frontend.
 * - Embed GroupDTOs to show course groups without exposing JPA internals or bidirectional links.
 */
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private List<GroupDTO> groups = new ArrayList<>();

    /** No-arg constructor used by the JSON library. */
    public CourseDTO() {}

    /** Simple constructor used by the mapper. */
    public CourseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Groups that belong to this course (as GroupDTO). */
    public List<GroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDTO> groups) {
        this.groups = groups;
    }
}
