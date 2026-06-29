package be.ucll.dto;

import be.ucll.model.Student;

import java.util.List;

public class StudentExcelParseResult {
    private List<Student> processedStudents;
    private List<String> nonStandardStudentNumbers;

    public StudentExcelParseResult(List<Student> processedStudents, List<String> nonStandardStudentNumbers) {
        this.processedStudents = processedStudents;
        this.nonStandardStudentNumbers = nonStandardStudentNumbers;
    }

    public List<Student> getProcessedStudents() {
        return processedStudents;
    }

    public List<String> getNonStandardStudentNumbers() {
        return nonStandardStudentNumbers;
    }
}
