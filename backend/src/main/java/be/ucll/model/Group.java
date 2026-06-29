package be.ucll.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Group.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "GROUPTABLE")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @ManyToMany(mappedBy = "assignedGroups")
    @JsonIgnore
    private List<Student> assignedStudents = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    private Course course;

    public Group(String name, Course course) {
        setName(name);
        setCourse(course);
    }

    public Group() {
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

    public List<Student> getAssignedStudents() {
        return assignedStudents;
    }

    public void addStudent(Student student) {
        if (!assignedStudents.contains(student)) {
            this.assignedStudents.add(student);
            student.addToGroup(this);
        }

    }

    public void removeStudent(Student student) {
        this.assignedStudents.remove(student);
    }

    public Course getCourse() {
        return this.course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
