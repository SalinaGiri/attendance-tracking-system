package be.ucll.dto;

import be.ucll.model.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Event entities.
 *
 * Purpose:
 * - Represents an Event in API responses with only the fields relevant to the frontend.
 * - Includes basic course info (id + name) and a list of GroupDTOs.
 *
 * Notes:
 * - Dates are represented as LocalDateTime; Jackson will serialize them according to the configured mapper.
 */
public class EventDTO {
    private Long id;
    private String eventName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private EventType  eventType;
    private Long courseId;
    private String courseName;
    private List<GroupDTO> groups = new ArrayList<>();

    /** No-arg constructor for Jackson. */
    public EventDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /** Event start time. */
    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    /** Event end time. */
    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /** Course id of the course that owns this event. */
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    /** Optional course name for convenience on the frontend. */
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /** Groups participating in this event (as GroupDTO). */
    public List<GroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDTO> groups) {
        this.groups = groups;
    }
}
