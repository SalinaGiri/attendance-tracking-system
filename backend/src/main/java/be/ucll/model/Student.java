package be.ucll.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "studentNumber", scope = Student.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "students")
@Entity
public class Student {
    @Id
    @Column(name = "STUDENT_NUMBER")
    private String studentNumber;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @ManyToMany
    @JoinTable(
            name = "STUDENT_IN_GROUP",
        joinColumns = @JoinColumn(name = "STUDENTNUMBER", referencedColumnName = "STUDENT_NUMBER"),
        inverseJoinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")
    )
    private List<Group> assignedGroups = new ArrayList<>();

    public Student() {
    }

    public Student(String firstName, String lastName, String studentNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        setStudentNumber(studentNumber);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getLastName() {return lastName;}

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentNumber() {return studentNumber;}

    public void setStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.isBlank()) {
            throw new RuntimeException("Student number cannot be null or empty");
        }
        this.studentNumber = studentNumber;
    }

    public List<Group> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(List<Group> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public void addToGroup(Group group){
        if (!assignedGroups.contains(group)) {
            this.assignedGroups.add(group);
            group.addStudent(this);
        }
    }

    public void removeFromGroup(Group group){
        this.assignedGroups.remove(group);
    }
}
