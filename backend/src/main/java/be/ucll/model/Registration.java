package be.ucll.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private RegistrationType type;

    @ManyToOne
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    private StatusType status = StatusType.Absent;

    private boolean validAbsence = false;

    @Column(name = "STUDENTNUMBER")
    private String studentNumber;
    
    @Column(name = "FIRSTNAME")
    private String firstName;

    @Column(name = "LASTNAME")
    private String lastName;

    @Column(name = "EMAIL")
    private String email;

    public Registration() {
    }

    public Registration(LocalDateTime date, RegistrationType type, Event event, Student student, StatusType status) {
        setDate(date);
        setType(type);
        setEvent(event);
        setStudent(student);
        setStatus(status);
        setStudentNumber(student.getStudentNumber());
        setFirstName(student.getFirstName());
        setLastName(student.getLastName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isValidAbsence() {
        return validAbsence;
    }

    public void setValidAbsence(boolean validAbsence) {
        this.validAbsence = validAbsence;
    }

    public void toggleValidAbsence() {
        this.validAbsence = !this.validAbsence;
    }

    public RegistrationType getType() {
        return type;
    }

    public void setType(RegistrationType type) {
        this.type = type;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        if (status == StatusType.Unexpected) {
            this.status = StatusType.Unexpected;
        } else {
            LocalDateTime referenceTime = null;
            if (type == RegistrationType.Checkin) {
                referenceTime = event.getCheckInTime();
            } else if (type == RegistrationType.Checkout) {
                referenceTime = event.getCheckOutTime();
            }

            if (date == null) {
                this.status = StatusType.Absent;
            } else if (date.isAfter(referenceTime)) {
                if (type == RegistrationType.Checkin) {
                    this.status = StatusType.Late;
                }
                if (type == RegistrationType.Checkout) {
                    this.status = StatusType.Present;
                }
            } else {
                this.status = StatusType.Present;
            }
        }
    }
}

