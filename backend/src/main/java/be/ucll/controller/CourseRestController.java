package be.ucll.controller;

import be.ucll.controller.DTO.CourseInput;
import be.ucll.dto.*;
// DTO imports used where needed; avoid unused imports
import be.ucll.model.Course;
import be.ucll.model.Group;
import be.ucll.model.Student;
import be.ucll.service.CourseService;
import be.ucll.service.EventService;
import be.ucll.service.FileValidationService;
import be.ucll.service.GroupService;
import be.ucll.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class CourseRestController {
    private final CourseService courseService;
    private final EventService eventService;
    private final StudentService studentService;
    private final FileValidationService fileValidationService;
    private final GroupService groupService;

    public CourseRestController(
            CourseService courseService,
            EventService eventService,
            StudentService studentService,
            FileValidationService fileValidationService,
            GroupService groupService) {
        this.courseService = courseService;
        this.eventService = eventService;
        this.studentService = studentService;
        this.fileValidationService = fileValidationService;
        this.groupService = groupService;
    }

    @GetMapping
    public List<CourseDTO> returnAllCourses() {
        return courseService.getAllCourses().stream().map(DtoMapper::toCourseDTO).collect(Collectors.toList());
    }


    @PostMapping
    public Course addCourse(@RequestBody @Valid CourseInput courseData) {

        return courseService.addCourse(courseData.name(), courseData.description());
    }

    @PostMapping("{courseId}/groups/{groupName}/students")
    public ResponseEntity<Map<String, Object>> addCourseGroupWithFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable(value = "courseId") Long courseId,
            @PathVariable(value = "groupName") String groupName
    ) {
        Map<String, Object> response = new HashMap<>();


        try {
            fileValidationService.validateExcelFile(file);

            StudentExcelParseResult studentExcelParseResult = studentService.parseStudentExcel(file);
            List<Student> students = studentExcelParseResult.getProcessedStudents();
            if (students.isEmpty()) {
                response.put("error", "No valid students found in the Excel file. Course not created.");
                return ResponseEntity.badRequest().body(response);
            }

            Course course = courseService.getCourse(courseId);
            studentService.addStudentsToGroup(students, groupName, course);

            response.put("message", "Required students uploaded successfully");
            response.put("course", DtoMapper.toCourseDTO(course));
            response.put("requiredGroupName", groupName);
            response.put("studentCount", students.size());
            response.put("students", students.stream().map(DtoMapper::toStudentDTO).collect(Collectors.toList()));
            response.put("nonStandardStudentNumbers", studentExcelParseResult.getNonStandardStudentNumbers());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to create course or process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{courseId}/groups")
    public List<GroupCountDTO> getGroupsWithStudentCounts(@PathVariable Long courseId) {
        List<Group> groups = courseService.findGroupsByCourseId(courseId);
        List<GroupCountDTO> groupDTOs = new ArrayList<>();

        for (Group g : groups) {
            groupDTOs.add(DtoMapper.toGroupCountDTO(g));
        }

        return groupDTOs;
    }

    @PostMapping("{courseId}/groups")
    public Group createGroup(@PathVariable Long courseId,
                             @RequestBody String groupName) {
        return courseService.createGroupForCourse(courseId, groupName);
    }

    @GetMapping("{courseId}/groups/{groupName}")
    public List<StudentDTO> getStudentsFromCourseGroup(
            @PathVariable Long courseId,
            @PathVariable String groupName) {

        List<Student> students = courseService.findStudentsFromGroup(courseId, groupName);
        List<StudentDTO> studentDTOs = new ArrayList<>();

        for (Student s : students) {
            studentDTOs.add(DtoMapper.toStudentDTO(s));
        }

        return studentDTOs;
    }

    @PostMapping("{courseId}/groups/{groupName}/students/manual")
    public ResponseEntity<Map<String, Object>> addStudentManually(
            @PathVariable Long courseId,
            @PathVariable String groupName,
            @RequestBody StudentDTO studentDTO
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course course = courseService.getCourse(courseId);
            Student student = studentService.addStudentToGroupManually(studentDTO, groupName, course);
            response.put("message", "Student added successfully");
            response.put("student", DtoMapper.toStudentDTO(student));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to add student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("{courseId}/groups/{groupName}/students/{studentNumber}")
    public ResponseEntity<Map<String, Object>> removeStudentFromGroup(
            @PathVariable Long courseId,
            @PathVariable String groupName,
            @PathVariable String studentNumber
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course course = courseService.getCourse(courseId);
            courseService.removeStudentFromGroup(studentNumber, groupName, course);
            response.put("message", "Student removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to remove student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{courseId}/groups/{groupName}/count")
    public GroupCountDTO getGroupCount(@PathVariable Long courseId,
                                       @PathVariable String groupName) {
        Group group = courseService.findGroupByNameInCourse(courseId, groupName);
        return DtoMapper.toGroupCountDTO(group);
    }

    @DeleteMapping("/{courseId}/groups/{groupName}")
    public String deleteGroup(@PathVariable Long courseId,
                              @PathVariable String groupName) {
        return courseService.deleteGroupFromCourse(courseId, groupName);
    }


    @PostMapping("{courseId}/create-groups")
    public CourseDTO addGroupsToCourse(@PathVariable Long courseId, @RequestBody List<Group> groups) {
        Course updated = courseService.addGroupsToCourse(courseId, groups);
        return DtoMapper.toCourseDTO(updated);
    }

    @GetMapping("/{courseId}/events")
    public List<EventDTO> findEventsByCourseId(@PathVariable Long courseId) {
        return eventService.findEventsByCourseId(courseId).stream().map(DtoMapper::toEventDTO).toList();
    }

    @GetMapping("/{id}")
    public CourseDTO getCourse(@PathVariable("id") Long id) {
        return DtoMapper.toCourseDTO(courseService.getCourse(id));
    }

    @PostMapping("/{id}/expected-attendees/group/{name}")
    public ResponseEntity<Map<String, Object>> uploadRequiredAttendees(
            @PathVariable("id") Long courseId,
            @PathVariable("name") String groupName,
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            fileValidationService.validateExcelFile(file);

            // Find the group by name first
            Group group = groupService.findGroupByName(groupName);

            // Process the Excel file and add students to the group
            List<Student> addedStudents = groupService.processStudentExcelForGroup(file, group.getId(), courseId);

            response.put("message", "Required attendees uploaded successfully");
            response.put("count", addedStudents.size());
            response.put("students", addedStudents.stream().map(DtoMapper::toStudentDTO).collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
