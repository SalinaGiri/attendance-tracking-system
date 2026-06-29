package be.ucll.service;

import be.ucll.dto.RegistrationDTOV2;
import be.ucll.model.*;
import be.ucll.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;
    private final RegistrationDTOV2Repository registrationDTOV2Repository;

    public EventService(EventRepository eventRepository, RegistrationRepository registrationRepository,
                        StudentRepository studentRepository, GroupRepository groupRepository,
                        RegistrationDTOV2Repository registrationDTOV2Repository) {
            this.eventRepository = eventRepository;
            this.registrationRepository = registrationRepository;
            this.studentRepository = studentRepository;
            this.groupRepository = groupRepository;
            this.registrationDTOV2Repository = registrationDTOV2Repository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public Event createEvent(Event event) {
        // Saving event immediately because it's going to be referenced by registrations, and it's not allowed to
        // reference a transient entity.
        eventRepository.save(event);

        // Using HashMap so that the students do not get duplicated
        // if they're present in more than one group
        HashMap<String, Student> students = new HashMap<>();

        for (Group group : event.getGroups()) {
            // the data of a given group is lost when retrieving it because of @JsonIgnore (I assume),
            // so we're getting its entire data here
            group = groupRepository.findById(group.getId()).get();

            for (Student student : group.getAssignedStudents()) {
                // put method will overwrite the previous value if present
                // so a student that is present more than one time is added only once
                students.put(student.getStudentNumber(), student);
            }
        }

        ArrayList<Registration> registrations = new ArrayList<>();
        for (Student student : students.values()) {
            Student studentFromJPA = studentRepository.findByStudentNumber(student.getStudentNumber());
            Registration checkInRegistration = null;
            Registration checkOutRegistration = null;
            if (event.getCheckInTime() != null) {
                checkInRegistration = new Registration(null, RegistrationType.Checkin, event, studentFromJPA, StatusType.Absent);
                registrations.add(checkInRegistration);
                registrationRepository.save(checkInRegistration);
            }
            if (event.getCheckOutTime() != null) {
                checkOutRegistration = new Registration(null, RegistrationType.Checkout, event, studentFromJPA, StatusType.Absent);
                registrations.add(checkOutRegistration);
                registrationRepository.save(checkOutRegistration);
            }
            RegistrationDTOV2 registrationDTOV2 = new RegistrationDTOV2(checkInRegistration, checkOutRegistration, event);
            registrationDTOV2Repository.save(registrationDTOV2);
        }
        return event;
    }

    public List<Event> findEventsByCourseId(Long id) {
        return eventRepository.findEventsByCourseId(id);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Registration> getActualAttendees(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }

        List<Registration> present = registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Present);
        List<Registration> late = registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Late);

        present.addAll(late);
        return present;
    }

    public List<Registration> getStudentsByRegistrationType(Long eventId, RegistrationType type) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return registrationRepository.findByEvent_IdAndType(eventId, type);
    }

    /**
     * Return students who were expected for the event and actually attended (present).
     * Expected students are those assigned to groups that belong to the event's course.
     */
    public List<Student> getExpectedPresentAttendees(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        Long courseId = event.getCourse().getId();

        // students expected for this event (in groups of the course)
        List<Student> expected = studentRepository.findDistinctByAssignedGroups_Course_Id(courseId);

        // students who registered and attended for this event
        List<Registration> attended = new ArrayList<>();
        attended.addAll(registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Present));
        attended.addAll(registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Late));

        Set<String> attendedNumbers = attended.stream()
            .map(r -> r.getStudent().getStudentNumber())
            .collect(Collectors.toSet());


        return expected.stream()
            .filter(s -> attendedNumbers.contains(s.getStudentNumber()))
            .collect(Collectors.toList());
    }

    /**
     * Return students who were expected for the event but did NOT attend (absent).
     */
    public List<Student> getExpectedAbsentAttendees(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        Long courseId = event.getCourse().getId();

        List<Student> expected = studentRepository.findDistinctByAssignedGroups_Course_Id(courseId);

        List<Registration> attended = new ArrayList<>();
        attended.addAll(registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Present));
        attended.addAll(registrationRepository.findByEvent_IdAndStatus(eventId, StatusType.Late));

        Set<String> attendedNumbers = attended.stream()
            .map(r -> r.getStudent().getStudentNumber())
            .collect(Collectors.toSet());


        return expected.stream()
                .filter(s -> !attendedNumbers.contains(s.getStudentNumber()))
                .collect(Collectors.toList());
        }

     public String changeEventRotationCode(Long eventId) {
         Optional<Event> event = eventRepository.findById(eventId);
         if (event.isEmpty()) { throw new EntityNotFoundException("Event not found with id: " + eventId); }

         int length = 6;
         boolean useLetters = false;
         boolean useNumbers = true;
         String rotationCode = RandomStringUtils.random(length, useLetters, useNumbers);

         event.get().setEventRotationCode(rotationCode);
         eventRepository.save(event.get());
         return event.get().getEventRotationCode();
     }

    public List<Group> getGroupsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        List<Group> groups = event.getGroups();
        groups.forEach(group -> {
            int size = group.getAssignedStudents().size();
            System.out.println(size);
        });
        return groups;
    }
}
