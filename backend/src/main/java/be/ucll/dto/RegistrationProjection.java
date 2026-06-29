package be.ucll.dto;

public interface RegistrationProjection {
    String getStudentNumber();
    String getFirstName();
    String getLastName();
    Long getAbsenceCount();
    Long getValidAbsenceCount();
    Long getHalfPresentCount();
    Long getUnexpectedCount();
    Long getLateCount();
    Long getAbsentLateCount();
}
