package be.ucll.dto;

/**
 * Data Transfer Object for Student.
 *
 * Purpose:
 * - Defines the JSON shape sent to the frontend for Student resources.
 * - Keeps the API surface small and stable compared to exposing JPA entities directly.
 *
 * Notes:
 * - Contains only the fields required by the UI (studentNumber, firstName, lastName).
 * - Mapping between entity and this DTO is handled in {@link be.ucll.dto.DtoMapper}.
 */
public class StudentDTO {
    private String studentNumber;
    private String firstName;
    private String lastName;

    /** No-arg constructor required by Jackson for deserialization. */
    public StudentDTO() {
    }

    /**
     * Create a StudentDTO.
     * @param studentNumber unique student id
     * @param firstName given name
     * @param lastName family name
     */
    public StudentDTO(String studentNumber, String firstName, String lastName) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /** Unique identifier for the student (e.g. r1234567). */
    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    /** First/given name of the student. */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Last/family name of the student. */
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
