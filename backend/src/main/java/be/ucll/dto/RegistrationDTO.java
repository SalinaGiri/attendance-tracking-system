package be.ucll.dto;

import java.io.ObjectInputFilter.Status;
import java.time.LocalDateTime;

import be.ucll.model.Registration;
import be.ucll.model.RegistrationType;
import be.ucll.model.StatusType;

/**
 * Clean DTO for Registration responses that excludes nested event/group/student details
 */
public class RegistrationDTO {
    private Long id;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime date;
    private RegistrationType type;
    private StatusType status;
    private boolean validAbsence;

    public RegistrationDTO() {
    }

    public RegistrationDTO(Registration registration) {
        this.id = registration.getId();
        this.studentNumber = registration.getStudentNumber();
        this.firstName = registration.getFirstName();
        this.lastName = registration.getLastName();
        this.email = registration.getEmail();
        this.date = registration.getDate();
        this.type = registration.getType();
        this.status = registration.getStatus();
        this.validAbsence = registration.isValidAbsence();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public RegistrationType getType() {
        return type;
    }

    public void setType(RegistrationType type) {
        this.type = type;
    }
    
    public boolean isValidAbsence() {
        return validAbsence;
    }

    public void setValidAbsence(boolean validAbsence) {
        this.validAbsence = validAbsence;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }
}
