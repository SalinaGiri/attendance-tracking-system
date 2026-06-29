package be.ucll.service;

import be.ucll.model.Course;
import be.ucll.model.Event;
import be.ucll.model.Group;
import be.ucll.model.Student;
import be.ucll.repository.CourseRepository;
import be.ucll.repository.EventRepository;
import be.ucll.repository.GroupRepository;
import be.ucll.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         GroupService groupService,
                         GroupRepository groupRepository,
                         StudentRepository studentRepository, EventRepository eventRepository) {
        this.courseRepository = courseRepository;
        this.groupService = groupService;
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
    }

    public Course addGroupsToCourse(Long courseId, List<Group> groups) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course with id " + courseId + " not found."));

        for (Group newGroup : groups) {
            boolean existsInCourse = course.getGroups().stream()
                    .anyMatch(g -> g.getName().equalsIgnoreCase(newGroup.getName()));
            if (!existsInCourse) {
                newGroup.setCourse(course);
                course.getGroups().add(newGroup);
            }
        }
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course addCourse(String name, String description) {
        Optional<Course> existingCourse = courseRepository.findByName(name);
        if (existingCourse.isPresent()) {
            throw new RuntimeException("Course already exists");
        }
        return courseRepository.save(new Course(name, description));
    }

    public Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course with id " + courseId + " not found."));
    }


    public List<Group> findGroupsByCourseId(Long courseId) {
        Course course = getCourse(courseId);

        return course.getGroups();
    }

    public Group createGroupForCourse(Long courseId, String groupName) {
        Course course = getCourse(courseId);

        return groupService.findOrCreateGroupByName(groupName, course);
    }

    public List<Student> findStudentsFromGroup(Long courseId, String groupName) {
        Course course = getCourse(courseId);

        if (course.getGroups().isEmpty()){
            throw new RuntimeException("Group " + groupName  + " does not exist.");
        }

        for (Group group : course.getGroups()){
            if (group.getName().equals(groupName)){
                return group.getAssignedStudents();
            }
        }
        throw new RuntimeException("Group " + groupName  + " does not exist.");
    }

    public Group findGroupByNameInCourse(Long courseId, String groupName) {
        Course course = getCourse(courseId);
        return course.getGroups().stream()
                .filter(g -> g.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Group " + groupName + " not found in course " + courseId));
    }

    public String deleteGroupFromCourse(Long courseId, String groupName){
        Course course = getCourse(courseId);
        Group group = findGroupByNameInCourse(courseId, groupName);
        course.getGroups().remove(group);
        for (Student student : group.getAssignedStudents()) {
            student.getAssignedGroups().remove(group);
        }
        List<Event> events = eventRepository.findAllByGroupsContains(group);
        for (Event event : events) {
            event.getGroups().remove(group);
            eventRepository.save(event);
        }
        groupRepository.delete(group);
        return "Deleted group " + groupName + " successfully.";
    }

    public void removeStudentFromGroup(String studentNumber, String groupName, Course course) {
        Student student = studentRepository.findById(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Group group = findGroupByNameInCourse(course.getId(), groupName);

        student.getAssignedGroups().remove(group);
        group.getAssignedStudents().remove(student);

        groupService.saveGroup(group);
        studentRepository.save(student);
    }
}
