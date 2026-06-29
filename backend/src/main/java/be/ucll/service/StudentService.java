package be.ucll.service;

import be.ucll.dto.StudentDTO;
import be.ucll.dto.StudentExcelParseResult;
import be.ucll.model.Course;
import be.ucll.model.Group;
import be.ucll.model.Student;
import be.ucll.repository.StudentRepository;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final GroupService groupService;

    @Autowired
    public StudentService(StudentRepository studentRepository, GroupService groupService) {
        this.studentRepository = studentRepository;
        this.groupService = groupService;
    }

    public boolean existsInDatabase(Student student) {
        return studentRepository.existsByStudentNumber(student.getStudentNumber());
    }

    public List<Student> addStudents(@Valid List<Student> students) {
        for (Student student : students) {
            if (existsInDatabase(student)) {
                throw new RuntimeException(
                        "Student " + student.getFirstName() + " " + student.getLastName() + " already exists in the database.");
            }
        }
        List<Student> savedStudents = new ArrayList<>();
        for (Student student : students) {
            savedStudents.add(studentRepository.save(student));
        }
        return savedStudents;
    }

    public List<Student> addStudentsToGroup(@Valid List<Student> students, String groupName, Course course) {
        Group group = groupService.findOrCreateGroupByName(groupName, course);
        if (group == null) {
            group = new Group();
            group.setName(groupName);
            group = groupService.saveGroup(group);
        }
        for (Student student : students) {
            boolean isNewToGroup = !student.getAssignedGroups().contains(group);
            student.addToGroup(group);
            group.addStudent(student);
            
            // Create registrations for existing events if student is newly added to the group
            if (isNewToGroup) {
                groupService.createRegistrationsForStudent(student, group);
            }
        }

        return studentRepository.saveAll(students);
    }

    public Student addStudentToGroupManually(StudentDTO studentDTO, String groupName, Course course) {
        Optional<Student> optStudent = studentRepository.findById(studentDTO.getStudentNumber());
        Student student;

        if (optStudent.isPresent()) {
            student = optStudent.get();
            student.setFirstName(studentDTO.getFirstName());
            student.setLastName(studentDTO.getLastName());
        } else {
            student = new Student();
            student.setStudentNumber(studentDTO.getStudentNumber());
            student.setFirstName(studentDTO.getFirstName());
            student.setLastName(studentDTO.getLastName());
            student = studentRepository.save(student); // save new student first
        }

        Group group = groupService.findOrCreateGroupByName(groupName, course);

        boolean isNewToGroup = !student.getAssignedGroups().contains(group);
        
        if (isNewToGroup) {
            student.getAssignedGroups().add(group);
        }
        if (!group.getAssignedStudents().contains(student)) {
            group.getAssignedStudents().add(student);
        }

        groupService.saveGroup(group);
        
        // Create registrations for existing events if student is newly added to the group
        if (isNewToGroup) {
            groupService.createRegistrationsForStudent(student, group);
        }
        
        return student;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsFromGroup(String groupName) {
        Group group = groupService.findGroupByName(groupName);
        return group.getAssignedStudents();
    }

    public StudentExcelParseResult parseStudentExcel(MultipartFile file) {
        List<Student> students = new ArrayList<>();
        List<String> nonStandardStudentNumbers = new ArrayList<>();


        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next();

            int rowNumber = 2;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String firstName = getCellValue(row, 3);
                String lastName = getCellValue(row, 2);
                String studentEmail = getCellValue(row, 4);
                String studentNumber = getCellValue(row, 7);

                if (lastName.isEmpty() && firstName.isEmpty() && studentNumber.isEmpty()) {
                    break;
                }

                try {
                    if (lastName.isEmpty() || firstName.isEmpty() || studentNumber.isEmpty()) {
                        throw new RuntimeException("Incomplete data at row " + rowNumber +
                                ": lastName='" + lastName + "', firstName='" + firstName + "', studentNumber='" + studentNumber + "'");
                    }

                    if (!studentNumber.matches("r\\d{7}")) {
                        nonStandardStudentNumbers.add(studentNumber);
                    }

                    Optional<Student> student = studentRepository.findById(studentNumber);
                    if (student.isEmpty()){
                        students.add(new Student(firstName, lastName, studentNumber));
                    } else {
                        students.add(student.get());
                    }

                } catch (Exception e) {
                    System.out.println("Skipping row " + rowNumber + " due to error: " + e.getMessage());
                }

                rowNumber++;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return new StudentExcelParseResult(students, nonStandardStudentNumbers);
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
}
