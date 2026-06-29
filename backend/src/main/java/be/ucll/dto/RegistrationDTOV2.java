package be.ucll.dto;

import be.ucll.model.Event;
import be.ucll.model.Registration;
import be.ucll.model.RegistrationType;
import be.ucll.model.StatusType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;


/**
 * Clean DTO for Registration responses that excludes nested event/group/student details
 */

@Entity
public class RegistrationDTOV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long checkInId;
    private Long checkOutId;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private long timeDelta = 0;
    @Enumerated(EnumType.STRING)
    private RegistrationType type;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @Enumerated(EnumType.STRING)
    private StatusType checkInStatus;
    @Enumerated(EnumType.STRING)
    private StatusType checkOutStatus;
    private boolean validAbsence;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "event_id")
    private Event event;

    protected RegistrationDTOV2 () {}

    public RegistrationDTOV2(Registration registrationCheckIn, Registration registrationCheckOut,
                             Event event) {
        create(registrationCheckIn, registrationCheckOut, event);
    }

    private void setRegistrationValues(Registration registration) {
        this.studentNumber = registration.getStudentNumber();
        this.firstName = registration.getFirstName();
        this.lastName = registration.getLastName();
        this.email = registration.getEmail();
        this.type = registration.getType();
        this.validAbsence = registration.isValidAbsence();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }
    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }

    public Long getCheckOutId() {
        return checkOutId;
    }

    public void setCheckOutId(Long checkOutId) {
        this.checkOutId = checkOutId;
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

    public Event getEvent() {
        return event;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public long getTimeDelta() {
        return timeDelta;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public StatusType getCheckInStatus() {
        return checkInStatus;
    }

    public StatusType getCheckOutStatus() {
        return checkOutStatus;
    }

    private void setChildStatus(Registration registration) {
        if (registration.getType() == RegistrationType.Checkin) {
            this.checkInStatus = registration.getStatus();
        } else {
            this.checkOutStatus = registration.getStatus();
        }
        updateStatus();
    }

    public void eraseRegistrationData(RegistrationType registrationType) {
        if (registrationType == RegistrationType.Checkin) {
            checkInTime = null;
            checkInId = null;
            checkInStatus = StatusType.Absent;
        } else {
            checkOutTime = null;
            checkOutId = null;
            checkOutStatus = StatusType.Absent;
        }
        updateStatus();
    }

    public void setRegistrationData(Registration registration) {
        if (registration == null) {
            return;
        }
        if (registration.getType().equals(RegistrationType.Checkin)) {
            if (this.checkInTime == null || registration.getDate().isBefore(this.checkInTime)) {
                setCheckInTime(registration.getDate());
                setCheckInId(registration.getId());
            }
        } else {
            if (this.checkOutTime == null || registration.getDate().isAfter(this.checkOutTime)) {
                setCheckOutTime(registration.getDate());
                setCheckOutId(registration.getId());
            }
        }
        setChildStatus(registration);
        setTimeDelta();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
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

    private void setTimeDelta() {
        if (checkInTime != null && checkOutTime != null) {
            timeDelta = Duration.between(checkInTime, checkOutTime).getSeconds();
        }
    }

    public StatusType getStatus() {
        return status;
    }

    public void updateStatus() {
        HashSet<StatusType> statuses = new HashSet<>();
        statuses.add(getCheckInStatus());
        statuses.add(getCheckOutStatus());

        if (statuses.contains(StatusType.Unexpected)) {
            status = StatusType.Unexpected;
        } else if (statuses.contains(StatusType.Late) && statuses.contains(StatusType.Absent)) {
            status = StatusType.AbsentLate;
        } else if (statuses.contains(StatusType.Late)) {
            status = StatusType.Late;
        } else if (statuses.contains(StatusType.Present) && statuses.contains(StatusType.Absent)) {
            status = StatusType.HalfPresent;
        } else if (statuses.contains(StatusType.Present)){
            // setting to false because a present student isn't absent in the first place
            setValidAbsence(false);
            status = StatusType.Present;
        } else {
            status = StatusType.Absent;
        }
    }

    public void create(Registration registrationCheckIn, Registration registrationCheckOut, Event event) {
        setRegistrationData(registrationCheckIn);
        setRegistrationData(registrationCheckOut);
        setEvent(event);

        if (registrationCheckIn != null) {
            setRegistrationValues(registrationCheckIn);
        } else if (registrationCheckOut != null) {
            setRegistrationValues(registrationCheckOut);
        } else {
            throw new RuntimeException("Both registrations are null.");
        }
    }
}
