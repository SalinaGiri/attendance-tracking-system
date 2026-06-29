package be.ucll.service;

import be.ucll.dto.RegistrationDTOV2;
import be.ucll.model.*;
import be.ucll.repository.CourseRepository;
import be.ucll.repository.EventRepository;
import be.ucll.repository.GroupRepository;
import be.ucll.repository.RegistrationDTOV2Repository;
import be.ucll.repository.RegistrationRepository;
import be.ucll.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationDTOV2Repository registrationDTOV2Repository;

    @PersistenceContext
    private EntityManager entityManager;

    public GroupService(GroupRepository groupRepository, StudentRepository studentRepository, 
                       CourseRepository courseRepository, EventRepository eventRepository,
                       RegistrationRepository registrationRepository,
                       RegistrationDTOV2Repository registrationDTOV2Repository) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.registrationDTOV2Repository = registrationDTOV2Repository;
    }

    public Group findGroupByName(String groupName) {
        if (!groupRepository.existsByName(groupName)) {
            throw new RuntimeException("No group with that name found");
        }
        return groupRepository.findByName(groupName);
    }

    public Group findOrCreateGroupByName(String groupName, Course course) {
        Group group = groupRepository.findByCourseIdAndName(course.getId(), groupName);
        if (group == null) {
            group = new Group();
            group.setName(groupName);
            group.setCourse(course);
            group = groupRepository.save(group);
        }
        return group;
    }

    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    public List<Group> findAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional
    public List<Student> processStudentExcelForGroup(MultipartFile file, Long groupId, Long courseId) {
        // Validate that groupId and courseId are provided
        if (groupId == null) {
            throw new RuntimeException("Group ID is required for student upload");
        }
        if (courseId == null) {
            throw new RuntimeException("Course ID is required for student upload");
        }

        // Find the group and validate course
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

        // Verify that the course exists and the group belongs to the course
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found with ID: " + courseId);
        }

        if (!group.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Group does not belong to the specified course");
        }

        List<Student> studentsToAdd = parseStudentExcel(file);
        List<Student> addedStudents = new ArrayList<>();

        for (Student student : studentsToAdd) {
            // Check if student already exists by student number
            Student existingStudent = studentRepository.findByStudentNumber(student.getStudentNumber());

            if (existingStudent != null) {
                // Student exists, add them to the group if not already added
                if (!existingStudent.getAssignedGroups().contains(group)) {
                    // Insert into STUDENT_IN_GROUP with student number
                    insertStudentInGroup(group.getId(), existingStudent.getStudentNumber());
                    // Create registrations for existing events
                    createRegistrationsForStudent(existingStudent, group);
                    addedStudents.add(existingStudent);
                }
            } else {
                // Student doesn't exist, create and add to group
                Student savedStudent = studentRepository.save(student);
                // Insert into STUDENT_IN_GROUP with student number
                insertStudentInGroup(group.getId(), savedStudent.getStudentNumber());
                // Create registrations for existing events
                createRegistrationsForStudent(savedStudent, group);
                addedStudents.add(savedStudent);
            }
        }

        // Save the group with updated student relationships
        groupRepository.save(group);

        return addedStudents;
    }

    private List<Student> parseStudentExcel(MultipartFile file) {
        List<Student> students = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Read header row to get column mappings
            Map<String, Integer> columnMapping = new HashMap<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();

                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null) {
                        String columnName = getCellValue(headerRow, i).toLowerCase().trim();
                        if (!columnName.isEmpty()) {
                            columnMapping.put(columnName, i);
                        }
                    }
                }

                if (columnMapping.isEmpty()) {
                    throw new RuntimeException("No column headers found in the Excel file");
                }
            }

            int rowNumber = 2;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Get column indices from mapping
                Integer lastNameIdx = getColumnIndex(columnMapping, "naam", "lastname");
                Integer firstNameIdx = getColumnIndex(columnMapping, "voornaam","voornaam/roepnaam");
                Integer studentNumberIdx = getColumnIndex(columnMapping, "studentnumber", "inlognummer");

                // Fallback to default columns if specific headers not found
                if (lastNameIdx == null) lastNameIdx = 0;
                if (firstNameIdx == null) firstNameIdx = 1;
                if (studentNumberIdx == null) studentNumberIdx = 2;

                String lastName = getCellValue(row, lastNameIdx);
                String firstName = getCellValue(row, firstNameIdx);
                String studentNumber = getCellValue(row, studentNumberIdx);

                // Stop processing if we hit an empty row
                if (lastName.isEmpty() && firstName.isEmpty() && studentNumber.isEmpty()) {
                    break;
                }

                try {
                    if (lastName.isEmpty() || firstName.isEmpty() || studentNumber.isEmpty()) {
                        throw new RuntimeException("Incomplete data at row " + rowNumber +
                                ": lastName='" + lastName + "', firstName='" + firstName + "', studentNumber='" + studentNumber + "'");
                    }

                    // Validate student number format (should start with 'r' followed by 7 digits)
                    if (!studentNumber.matches("r\\d{7}")) {
                        throw new RuntimeException("Invalid student number format at row " + rowNumber + ": " + studentNumber);
                    }

                    Student student = new Student(lastName, firstName, studentNumber);
                    students.add(student);

                } catch (Exception e) {
                    System.out.println("Skipping row " + rowNumber + " due to error: " + e.getMessage());
                }

                rowNumber++;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return students;
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield String.valueOf((long) val);
            }
            default -> "";
        };
    }

    /**
     * Helper method to find column index by trying multiple possible column names
     */
    private Integer getColumnIndex(Map<String, Integer> columnMapping, String... possibleNames) {
        for (String name : possibleNames) {
            Integer index = columnMapping.get(name.toLowerCase());
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    /**
     * Insert student-group relationship with student number
     */
    private void insertStudentInGroup(Long groupId, String studentNumber) {
        String sql = "MERGE INTO STUDENT_IN_GROUP (GROUP_ID, STUDENTNUMBER) VALUES (?1, ?2)";
        entityManager.createNativeQuery(sql)
                .setParameter(1, groupId)
                .setParameter(2, studentNumber)
                .executeUpdate();
    }

    public List<Group> findGroupsByCourseId(Long courseId) {
        return groupRepository.findGroupsByCourse_Id(courseId);
    }

    /**
     * Creates registrations for a student for all existing events in a group.
     * If event is CheckInOut, creates both Checkin and Checkout registrations.
     * Registrations have null date and validAbsence set to true.
     * Also creates corresponding RegistrationDTOV2 entries.
     */
    public void createRegistrationsForStudent(Student student, Group group) {
        // Find all events for this group
        List<Event> events = eventRepository.findAllByGroupsContains(group);
        
        for (Event event : events) {
            EventType eventType = event.getEventType();
            Registration checkInRegistration = null;
            Registration checkOutRegistration = null;
            
            if (eventType == EventType.CheckIn) {
                // Create Checkin registration
                checkInRegistration = createRegistration(student, event, RegistrationType.Checkin);
            } else if (eventType == EventType.CheckOut) {
                // Create Checkout registration
                checkOutRegistration = createRegistration(student, event, RegistrationType.Checkout);
            } else if (eventType == EventType.CheckInOut) {
                // Create both Checkin and Checkout registrations
                checkInRegistration = createRegistration(student, event, RegistrationType.Checkin);
                checkOutRegistration = createRegistration(student, event, RegistrationType.Checkout);
            }
            
            // Create RegistrationDTOV2 entry
            RegistrationDTOV2 registrationDTOV2 = new RegistrationDTOV2(checkInRegistration, checkOutRegistration, event);
            registrationDTOV2Repository.save(registrationDTOV2);
        }
    }

    /**
     * Helper method to create a single registration with null date and validAbsence set to true
     * @return The created and saved Registration entity
     */
    private Registration createRegistration(Student student, Event event, RegistrationType type) {
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setEvent(event);
        registration.setType(type);
        registration.setDate(null);
        registration.setValidAbsence(true);
        registration.setStatus(StatusType.Absent);
        registration.setStudentNumber(student.getStudentNumber());
        registration.setFirstName(student.getFirstName());
        registration.setLastName(student.getLastName());
        
        return registrationRepository.save(registration);
    }

}
