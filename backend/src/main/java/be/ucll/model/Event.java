package be.ucll.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String eventName;

    @Column(name = "CHECK_IN_TIME")
    private LocalDateTime checkInTime;

    @Column(name = "CHECK_OUT_TIME")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @ManyToMany
    @JoinTable(name = "event_group",
    joinColumns = @JoinColumn(name = "event_id"),
    inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "rotation_code")
    private String rotationCode;

    protected Event() {}

    public Event(String eventName, LocalDateTime checkInTime, LocalDateTime checkOutTime,
                 List<Group> groups, Course course) {
        setEventName(eventName);
        setCheckInTime(checkInTime);
        setCheckOutTime(checkOutTime);
        setEventType();
        setCourse(course);
        setGroups(groups);
    }

    public Long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDateTime getCheckInTime() {
        setEventType();
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        setEventType();
        return checkOutTime;
    }

    public Course getCourse() {
        return course;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setEventName(String eventName) {
        if (eventName.isEmpty()) {
            throw new RuntimeException("Event name cannot be empty.");
        }
        this.eventName = eventName;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        if (checkInTime != null && checkOutTime != null && checkInTime.isAfter(checkOutTime)) {
            throw new RuntimeException("Check-in time cannot be after check-out time.");
        }
        this.checkOutTime = checkOutTime;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setGroups(List<Group> groups) {
        if (groups == null) {
            throw new RuntimeException("At least one group must be selected.");
        }
        this.groups = groups;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEventType() {
        if (checkInTime != null && checkOutTime != null) {
            this.eventType = EventType.CheckInOut;
        }

        if (checkInTime != null && checkOutTime == null) {
            this.eventType = EventType.CheckIn;
        }

        if (checkInTime == null && checkOutTime != null) {
           this.eventType = EventType.CheckOut;
        }
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getEventRotationCode() { return rotationCode;}

    public void setEventRotationCode(String rotationCode) { this.rotationCode = rotationCode;}

    public boolean isStudentExpected(String rnumber) {
        for (Group group : this.groups) {
            for (Student student: group.getAssignedStudents()) {
                if (student.getStudentNumber().equals(rnumber)) return true;
            }
        }

        return false;
    }
}
