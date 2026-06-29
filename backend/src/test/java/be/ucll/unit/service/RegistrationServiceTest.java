package be.ucll.unit.service;

import be.ucll.model.*;
import be.ucll.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RegistrationService focusing on filtering logic.
 * These tests focus on the core filtering algorithms without mocking.
 */
/* public class RegistrationServiceTest {

    private RegistrationService registrationService;

    private Event testEvent;
    private Student student1;
    private Student student2;
    private Student student3;
    private Course course;
    private Group group;

    @BeforeEach
    public void setUp() {
        // Create service instance without mocking
        registrationService = new RegistrationService(null, null, null, null);
        
        // Create a test course
        course = new Course("Test Course", "Test Description");
        course.setId(1L);

        // Create a test group
        group = new Group("Test Group", course);
        group.setId(1L);

        // Create test event with check-in and check-out times
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);
        testEvent = new Event("Test Event", checkInTime, checkOutTime, List.of(group), course);
        testEvent.setId(1L);

        // Create test students (r + 7 digits = 8 chars total)
        student1 = new Student("John", "Doe", "r0000001");
        student2 = new Student("Jane", "Smith", "r0000002");
        student3 = new Student("Bob", "Johnson", "r0000003");
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsPresent_BothCheckInAndCheckOut() {
        // Arrange: Student has both check-in and check-out on time
        LocalDateTime checkInDate = LocalDateTime.of(2025, 11, 28, 8, 55); // 5 min early
        LocalDateTime checkOutDate = LocalDateTime.of(2025, 11, 28, 16, 55); // 5 min early

        Registration checkInReg = new Registration(checkInDate, RegistrationType.Checkin, testEvent, student1, true);
        Registration checkOutReg = new Registration(checkOutDate, RegistrationType.Checkout, testEvent, student1, true);
        checkInReg.setStudentNumber("r0000001");
        checkOutReg.setStudentNumber("r0000001");

        List<Registration> registrations = List.of(checkInReg, checkOutReg);

        // Act: Filter for present students (not late, not absent)
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, false, false);

        // Assert: Should return the check-in registration for the present student
        assertEquals(1, result.size());
        assertEquals(RegistrationType.Checkin, result.get(0).getType());
        assertEquals("r0000001", result.get(0).getStudentNumber());
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsLate_CheckInAfterEventTime() {
        // Arrange: Student checked in late but checked out on time
        LocalDateTime lateCheckInDate = LocalDateTime.of(2025, 11, 28, 9, 30); // 30 min late
        LocalDateTime checkOutDate = LocalDateTime.of(2025, 11, 28, 16, 55);

        Registration checkInReg = new Registration(lateCheckInDate, RegistrationType.Checkin, testEvent, student1, true);
        Registration checkOutReg = new Registration(checkOutDate, RegistrationType.Checkout, testEvent, student1, true);
        checkInReg.setStudentNumber("r0000001");
        checkOutReg.setStudentNumber("r0000001");

        List<Registration> registrations = List.of(checkInReg, checkOutReg);

        // Act: Filter for late students
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, true, false);

        // Assert: Should return the check-in registration for the late student
        assertEquals(1, result.size());
        assertEquals(RegistrationType.Checkin, result.get(0).getType());
        assertEquals("r0000001", result.get(0).getStudentNumber());
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsLate_CheckOutAfterEventTime() {
        // Arrange: Student checked in on time but checked out late
        LocalDateTime checkInDate = LocalDateTime.of(2025, 11, 28, 8, 55);
        LocalDateTime lateCheckOutDate = LocalDateTime.of(2025, 11, 28, 17, 30); // 30 min late

        Registration checkInReg = new Registration(checkInDate, RegistrationType.Checkin, testEvent, student1, true);
        Registration checkOutReg = new Registration(lateCheckOutDate, RegistrationType.Checkout, testEvent, student1, true);
        checkInReg.setStudentNumber("r0000001");
        checkOutReg.setStudentNumber("r0000001");

        List<Registration> registrations = List.of(checkInReg, checkOutReg);

        // Act: Filter for late students
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, true, false);

        // Assert: Should return the check-in registration for the late student
        assertEquals(1, result.size());
        assertEquals("r0000001", result.get(0).getStudentNumber());
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsAbsent_MissingCheckIn() {
        // Arrange: Student only has check-out, missing check-in
        LocalDateTime checkOutDate = LocalDateTime.of(2025, 11, 28, 16, 55);

        Registration checkOutReg = new Registration(checkOutDate, RegistrationType.Checkout, testEvent, student1, true);
        checkOutReg.setStudentNumber("r0001");

        List<Registration> registrations = List.of(checkOutReg);

        // Act: Filter for absent students
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, false, true);

        // Assert: Should return the check-out registration since check-in is missing
        assertEquals(1, result.size());
        assertEquals(RegistrationType.Checkout, result.get(0).getType());
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsAbsent_MissingCheckOut() {
        // Arrange: Student only has check-in, missing check-out
        LocalDateTime checkInDate = LocalDateTime.of(2025, 11, 28, 8, 55);

        Registration checkInReg = new Registration(checkInDate, RegistrationType.Checkin, testEvent, student1, true);
        checkInReg.setStudentNumber("r0000001");

        List<Registration> registrations = List.of(checkInReg);

        // Act: Filter for absent students
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, false, true);

        // Assert: Should return the check-in registration since check-out is missing
        assertEquals(1, result.size());
        assertEquals(RegistrationType.Checkin, result.get(0).getType());
    }

    @Test
    public void testFilterRegistrationsByDate_StudentIsAbsent_NullCheckInDate() {
        // Arrange: Student has registrations but check-in date is null (year 1970)
        LocalDateTime nullDate = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime checkOutDate = LocalDateTime.of(2025, 11, 28, 16, 55);

        Registration checkInReg = new Registration(nullDate, RegistrationType.Checkin, testEvent, student1, true);
        Registration checkOutReg = new Registration(checkOutDate, RegistrationType.Checkout, testEvent, student1, true);
        checkInReg.setStudentNumber("r0000001");
        checkOutReg.setStudentNumber("r0000001");

        List<Registration> registrations = List.of(checkInReg, checkOutReg);

        // Act: Filter for absent students
        List<Registration> result = registrationService.filterRegistrationsByDate(registrations, false, true);

        // Assert: Should return the check-in registration for the absent student
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterRegistrations_MultipleStudents_MixedScenarios() {
        // Arrange: Multiple students with different scenarios
        LocalDateTime checkInDate1 = LocalDateTime.of(2025, 11, 28, 8, 55); // On time
        LocalDateTime checkOutDate1 = LocalDateTime.of(2025, 11, 28, 16, 55); // On time

        LocalDateTime lateCheckInDate2 = LocalDateTime.of(2025, 11, 28, 9, 30); // Late
        LocalDateTime checkOutDate2 = LocalDateTime.of(2025, 11, 28, 16, 55); // On time

        LocalDateTime checkInDate3 = LocalDateTime.of(2025, 11, 28, 8, 55); // On time
        // Student 3 missing check-out

        Registration student1CheckIn = new Registration(checkInDate1, RegistrationType.Checkin, testEvent, student1, true);
        Registration student1CheckOut = new Registration(checkOutDate1, RegistrationType.Checkout, testEvent, student1, true);
        student1CheckIn.setStudentNumber("r0000001");
        student1CheckOut.setStudentNumber("r0000001");

        Registration student2CheckIn = new Registration(lateCheckInDate2, RegistrationType.Checkin, testEvent, student2, true);
        Registration student2CheckOut = new Registration(checkOutDate2, RegistrationType.Checkout, testEvent, student2, true);
        student2CheckIn.setStudentNumber("r0000002");
        student2CheckOut.setStudentNumber("r0000002");

        Registration student3CheckIn = new Registration(checkInDate3, RegistrationType.Checkin, testEvent, student3, true);
        student3CheckIn.setStudentNumber("r0000003");

        List<Registration> registrations = List.of(
            student1CheckIn, student1CheckOut,
            student2CheckIn, student2CheckOut,
            student3CheckIn
        );

        // Act & Assert: Filter for present students
        List<Registration> presentStudents = registrationService.filterRegistrationsByDate(registrations, false, false);
        assertEquals(1, presentStudents.size()); // Only student1 is present and on time
        assertEquals("r0000001", presentStudents.get(0).getStudentNumber());

        // Act & Assert: Filter for late students
        List<Registration> lateStudents = registrationService.filterRegistrationsByDate(registrations, true, false);
        assertEquals(1, lateStudents.size()); // Only student2 is late
        assertEquals("r0000002", lateStudents.get(0).getStudentNumber());

        // Act & Assert: Filter for absent students
        List<Registration> absentStudents = registrationService.filterRegistrationsByDate(registrations, false, true);
        assertEquals(1, absentStudents.size()); // Only student3 is absent
        assertEquals("r0000003", absentStudents.get(0).getStudentNumber());
    }
}
 */