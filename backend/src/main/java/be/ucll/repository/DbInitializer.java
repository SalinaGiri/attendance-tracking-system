package be.ucll.repository;

import be.ucll.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DbInitializer {

    private final EventRepository eventRepository;
    private UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;


    @Autowired
    public DbInitializer(UserRepository userRepository,
                         EventRepository eventRepository,
                         GroupRepository groupRepository,
                         CourseRepository courseRepository,
                         StudentRepository studentRepository,
                         RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
    }

    @PostConstruct
    public void initialize() {
        // Users
        User user1 = new User("Tom Boon", 25, "tom.boon@ucll.be", "tom123");
        User user2 = new User("Loick Luypaert", 30, "loick.luypaert@ucll.be", "loick123");
        User user3 = new User("John Doe", 27, "john.doe@ucll.be", "john123");
        User user4 = new User("Jane Luypaert", 25, "jane.luypaert@ucll.be", "jane123");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        Student student1 = new Student("Alice", "Smith", "r0798252");
        studentRepository.save(student1);

        // Create a course first (required for events)
        Course course = new Course("Junior Project Course", "Course for junior workplace project events");
        Course course2 = new Course("Senior Workplace Project", "Course for third-years.");

        course = courseRepository.save(course);
        courseRepository.save(course2);

        Group allStudents = new Group("all", course);
        Group acsStudents = new Group("ACS", course2);
        Group tiStudents = new Group("TI", course);

        groupRepository.save(allStudents);
        groupRepository.save(acsStudents);
        groupRepository.save(tiStudents);

        List<Group> groups = new ArrayList<>();
        groups.add(allStudents);
        groups.add(tiStudents);

        Event event = new Event("Junior Workplace Project", LocalDateTime.now(), LocalDateTime.now().plusHours(8),
                groups, course);

        eventRepository.save(event);


        Registration registration1 = new Registration(LocalDateTime.now(), RegistrationType.Checkin, event, student1, StatusType.Present);
        registration1.setStudentNumber(student1.getStudentNumber());
        registration1.setFirstName(student1.getFirstName());
        registration1.setLastName(student1.getLastName());
        registrationRepository.save(registration1);
    }
}
