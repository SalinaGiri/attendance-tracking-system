package be.ucll.controller;

import be.ucll.dto.DtoMapper;
import be.ucll.dto.StudentDTO;
import be.ucll.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class StudentRestController {
    private final StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{groupName}")
    public List<StudentDTO> addCourse(@PathVariable(value = "groupName") String groupName) {
        return studentService.getStudentsFromGroup(groupName).stream().map(DtoMapper::toStudentDTO).collect(Collectors.toList());
    }

    @GetMapping
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents().stream().map(DtoMapper::toStudentDTO).collect(Collectors.toList());
    }

}
