package be.ucll.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;


@Table(name = "courses")
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course name is required.")
    private String name;
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Group> groups = new ArrayList<>();

    protected Course() {
    }

    public Course(String name) {
        setName(name);
    }

    public Course(String name, String description) {
        setName(name);
        setDescription(description);
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

    public List<Group> getGroups() {
        return groups;
    }
    public void setGroups(List<Group> groups) {
        this.groups = groups;
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

    // For when we add manual group handling
    /*public void addGroup(Group group) {
        groups.add(group);
        group.setCourse(this);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        group.setCourse(null);
    }*/
}
