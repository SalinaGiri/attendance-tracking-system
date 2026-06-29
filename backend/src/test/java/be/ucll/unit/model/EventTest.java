package be.ucll.unit.model;

import be.ucll.model.Course;
import be.ucll.model.Event;
import be.ucll.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    private Course course;
    private Group group;

    @BeforeEach
    public void setUp() {
        course = new Course("Test Course", "Test Description");
        group = new Group("Test Group", course);
    }

    @Test
    public void testCreateEvent_ValidCheckInAndCheckOut() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);

        // Act
        Event event = new Event("Test Event", checkInTime, checkOutTime, List.of(group), course);

        // Assert
        assertNotNull(event);
        assertEquals("Test Event", event.getEventName());
        assertEquals(checkInTime, event.getCheckInTime());
        assertEquals(checkOutTime, event.getCheckOutTime());
    }

    @Test
    public void testCreateEvent_CheckInAfterCheckOut_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 17, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 9, 0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            new Event("Test Event", checkInTime, checkOutTime, List.of(group), course);
        }, "Check-in time cannot be after check-out time.");
    }

    @Test
    public void testSetCheckOutTime_AfterCheckIn_Success() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);
        Event event = new Event("Test Event", checkInTime, null, List.of(group), course);

        // Act
        event.setCheckOutTime(checkOutTime);

        // Assert
        assertEquals(checkOutTime, event.getCheckOutTime());
    }

    @Test
    public void testSetCheckOutTime_BeforeCheckIn_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime invalidCheckOutTime = LocalDateTime.of(2025, 11, 28, 8, 0);
        Event event = new Event("Test Event", checkInTime, null, List.of(group), course);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            event.setCheckOutTime(invalidCheckOutTime);
        }, "Check-in time cannot be after check-out time.");
    }

    @Test
    public void testSetCheckInTime_Null_Allowed() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);
        Event event = new Event("Test Event", null, checkOutTime, List.of(group), course);

        // Assert
        assertNull(event.getCheckInTime());
        assertEquals(checkOutTime, event.getCheckOutTime());
    }

    @Test
    public void testSetCheckOutTime_Null_Allowed() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        Event event = new Event("Test Event", checkInTime, null, List.of(group), course);

        // Assert
        assertEquals(checkInTime, event.getCheckInTime());
        assertNull(event.getCheckOutTime());
    }

    @Test
    public void testCreateEvent_EmptyEventName_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            new Event("", checkInTime, checkOutTime, List.of(group), course);
        }, "Event name cannot be empty.");
    }

    @Test
    public void testCreateEvent_NullGroups_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.of(2025, 11, 28, 9, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 11, 28, 17, 0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            new Event("Test Event", checkInTime, checkOutTime, null, course);
        }, "At least one group must be selected.");
    }

    @Test
    public void testCreateEvent_SameCheckInAndCheckOut_Success() {
        // Arrange: Same time for check-in and check-out (edge case)
        LocalDateTime sameTime = LocalDateTime.of(2025, 11, 28, 12, 0);

        // Act
        Event event = new Event("Test Event", sameTime, sameTime, List.of(group), course);

        // Assert
        assertNotNull(event);
        assertEquals(sameTime, event.getCheckInTime());
        assertEquals(sameTime, event.getCheckOutTime());
    }
}
