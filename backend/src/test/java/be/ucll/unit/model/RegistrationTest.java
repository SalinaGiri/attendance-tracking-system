/* package be.ucll.unit.model;

import be.ucll.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputFilter.Status;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationTest {

    private Event event;
    private Student student;
    private Course course;
    private Group group;

    @BeforeEach
    public void setUp() {
        course = new Course("Test Course", "Test Description");
        group = new Group("Test Group", course);
        
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);
        event = new Event("Test Event", checkInTime, checkOutTime, List.of(group), course);
        
        student = new Student("John", "Doe", "r0000001");
    }

    @Test
    public void testSetAttended_CheckinType_OnTime_SetsTrue() {
        // Arrange
        LocalDateTime registrationDate = LocalDateTime.of(2025, 11, 28, 8, 55); // 5 min early
        Registration registration = new Registration(registrationDate, RegistrationType.Checkin, event, student, true);

        // Act
        registration.setAttended(true);

        // Assert
        assertTrue(registration.isAttended(), "Student who checked in on time should be marked as attended");
    }

    @Test
    public void testSetAttended_CheckinType_Late_SetsFalse() {
        // Arrange
        LocalDateTime lateRegistrationDate = LocalDateTime.of(2025, 11, 28, 9, 30); // 30 min late
        Registration registration = new Registration(lateRegistrationDate, RegistrationType.Checkin, event, student, false);

        // Act
        registration.setAttended(true);

        // Assert
        assertFalse(registration.isAttended(), "Student who checked in late should not be marked as attended");
    }

    @Test
    public void testSetAttended_CheckoutType_OnTime_SetsTrue() {
        // Arrange
        LocalDateTime registrationDate = LocalDateTime.of(2025, 11, 28, 16, 55); // 5 min early
        Registration registration = new Registration(registrationDate, RegistrationType.Checkout, event, student, true);

        // Act
        registration.setAttended(true);

        // Assert
        assertTrue(registration.isAttended(), "Student who checked out on time should be marked as attended");
    }

    @Test
    public void testSetAttended_CheckoutType_Late_SetsFalse() {
        // Arrange
        LocalDateTime lateRegistrationDate = LocalDateTime.of(2025, 11, 28, 17, 30); // 30 min late
        Registration registration = new Registration(lateRegistrationDate, RegistrationType.Checkout, event, student, false);

        // Act
        registration.setAttended(true);

        // Assert
        assertFalse(registration.isAttended(), "Student who checked out late should not be marked as attended");
    }

    @Test
    public void testSetAttended_NullDate_SetsFalse() {
        // Arrange
        Registration registration = new Registration(null, RegistrationType.Checkin, event, student, false);

        // Act
        registration.setAttended(true);

        // Assert
        assertFalse(registration.isAttended(), "Registration with null date should not be marked as attended");
    }

    @Test
    public void testSetAttended_LunchCheckinType_OnTime_SetsTrue() {
        // Arrange
        LocalDateTime registrationDate = LocalDateTime.of(2025, 11, 28, 8, 55);
        Registration registration = new Registration(registrationDate, RegistrationType.LunchCheckin, event, student, true);

        // Act
        registration.setAttended(true);

        // Assert
        assertTrue(registration.isAttended(), "Student who did lunch check-in on time should be marked as attended");
    }

    @Test
    public void testSetAttended_LunchCheckoutType_Late_SetsFalse() {
        // Arrange
        LocalDateTime lateRegistrationDate = LocalDateTime.of(2025, 11, 28, 17, 30);
        Registration registration = new Registration(lateRegistrationDate, RegistrationType.LunchCheckout, event, student, false);

        // Act
        registration.setAttended(true);

        // Assert
        assertFalse(registration.isAttended(), "Student who did lunch check-out late should not be marked as attended");
    }

    @Test
    public void testToggleValidAbsence() {
        // Arrange
        LocalDateTime registrationDate = LocalDateTime.of(2025, 11, 28, 8, 55);
        Registration registration = new Registration(registrationDate, RegistrationType.Checkin, event, student, StatusType.Absent);
        registration.setValidAbsence(false);

        // Act
        registration.toggleValidAbsence();

        // Assert
        assertTrue(registration.isValidAbsence(), "Valid absence should toggle from false to true");

        // Act again
        registration.toggleValidAbsence();

        // Assert
        assertFalse(registration.isValidAbsence(), "Valid absence should toggle from true to false");
    }

    @Test
    public void testCreateRegistration_WithAllFields() {
        // Arrange & Act
        LocalDateTime registrationDate = LocalDateTime.of(2025, 11, 28, 8, 55);
        Registration registration = new Registration(registrationDate, RegistrationType.Checkin, event, student, StatusType.Present);
        registration.setStudentNumber("r0000001");
        registration.setFirstName("John");
        registration.setLastName("Doe");
        registration.setEmail("john.doe@example.com");

        // Assert
        assertEquals(registrationDate, registration.getDate());
        assertEquals(RegistrationType.Checkin, registration.getType());
        assertEquals(event, registration.getEvent());
        assertEquals(student, registration.getStudent());
        assertEquals("r0000001", registration.getStudentNumber());
        assertEquals("John", registration.getFirstName());
        assertEquals("Doe", registration.getLastName());
        assertEquals("john.doe@example.com", registration.getEmail());
    }
}
 */