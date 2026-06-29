package be.ucll.dto;

import be.ucll.model.Course;
import be.ucll.model.Event;
import be.ucll.model.Group;
import be.ucll.model.Student;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple mapper utility to convert JPA entities to their corresponding DTOs.
 *
 * Keep mapping logic centralized here so controllers remain thin and it's easy to
 * adapt the JSON shape later (for example when adding/removing fields).
 *
 * Note: mapping is currently one-way (entity -> DTO). If you want DTO -> entity
 * mapping for create/update operations, add corresponding methods here.
 */
public final class DtoMapper {
    private DtoMapper() {}

    /** Convert Student entity to StudentDTO. */
    public static StudentDTO toStudentDTO(Student s) {
        if (s == null) return null;
        return new StudentDTO(s.getStudentNumber(), s.getFirstName(), s.getLastName());
    }

    /** Convert Group entity to GroupDTO, including assigned students. */
    public static GroupDTO toGroupDTO(Group g) {
        if (g == null) return null;
        GroupDTO dto = new GroupDTO(g.getId(), g.getName(), g.getCourse() == null ? null : g.getCourse().getId());
        if (g.getAssignedStudents() != null) {
            List<StudentDTO> students = g.getAssignedStudents().stream()
                    .map(DtoMapper::toStudentDTO)
                    .collect(Collectors.toList());
            dto.setAssignedStudents(students);
        }
        return dto;
    }

    /** Convert Course entity to CourseDTO, including groups. */
    public static CourseDTO toCourseDTO(Course c) {
        if (c == null) return null;
        CourseDTO dto = new CourseDTO(c.getId(), c.getName(), c.getDescription());
        if (c.getGroups() != null) {
            List<GroupDTO> groups = c.getGroups().stream()
                    .map(DtoMapper::toGroupDTO)
                    .collect(Collectors.toList());
            dto.setGroups(groups);
        }
        return dto;
    }

    /** Convert Event entity to EventDTO, including course summary and groups. */
    public static EventDTO toEventDTO(Event e) {
        if (e == null) return null;
        EventDTO dto = new EventDTO();
        dto.setId(e.getId());
        dto.setEventName(e.getEventName());
        dto.setCheckInTime(e.getCheckInTime());
        dto.setCheckOutTime(e.getCheckOutTime());
        dto.setEventType(e.getEventType());
        if (e.getCourse() != null) {
            dto.setCourseId(e.getCourse().getId());
            dto.setCourseName(e.getCourse().getName());
        }
        if (e.getGroups() != null) {
            List<GroupDTO> groups = e.getGroups().stream()
                    .map(DtoMapper::toGroupDTO)
                    .collect(Collectors.toList());
            dto.setGroups(groups);
        }
        return dto;
    }

    //Same as GroupDTO but without the student list (has count instead)
    public static GroupCountDTO toGroupCountDTO(Group g) {
        if (g == null) return null;
        int count = g.getAssignedStudents() == null ? 0 : g.getAssignedStudents().size();
        Long courseId = g.getCourse() == null ? null : g.getCourse().getId();
        return new GroupCountDTO(g.getId(), g.getName(), courseId, count);
    }
}
