package be.ucll.repository;

import be.ucll.dto.RegistrationDTOV2;
import be.ucll.dto.RegistrationProjection;
import be.ucll.model.RegistrationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationDTOV2Repository extends JpaRepository<RegistrationDTOV2, Long> {
    List<RegistrationDTOV2> findByEvent_Id(Long eventId);
    Optional<RegistrationDTOV2> findByEvent_IdAndStudentNumber(Long eventId, String studentNumber);
    Optional<RegistrationDTOV2> findByCheckInId(Long checkInId);
    Optional<RegistrationDTOV2> findByCheckOutId(Long checkOutId);

    void deleteByCheckInId(Long id);
    void deleteByCheckOutId(Long id);

    @Query(value = "SELECT \n" +
            "    r.studentnumber,\n" +
            "    MAX(r.firstname) AS firstname,\n" +
            "    MAX(r.lastname) AS lastname,\n" +
            "    SUM(CASE WHEN r.status = 'Absent' THEN 1 ELSE 0 END) AS absenceCount,\n" +
            "    SUM(CASE WHEN r.validabsence = TRUE THEN 1 ELSE 0 END) AS validAbsenceCount,\n" +
            "    SUM(CASE WHEN r.status = 'HalfPresent' THEN 1 ELSE 0 END) AS halfPresentCount,\n" +
            "    SUM(CASE WHEN r.status = 'Unexpected' THEN 1 ELSE 0 END) AS unexpectedCount,\n" +
            "    SUM(CASE WHEN r.status = 'Late' OR r.status = 'HalfPresentLate' THEN 1 ELSE 0 END) as lateCount\n," +
            "    SUM(CASE WHEN r.status = 'AbsentLate' THEN 1 ELSE 0 END) as absentLateCount\n" +
            "FROM REGISTRATIONDTOV2 r\n" +
            "JOIN EVENTS e ON r.event_id = e.id\n" +
            "WHERE r.status <> 'Present'\n" +
            "  AND e.course_id = :courseId\n" +
            "GROUP BY r.studentnumber;\n",
            nativeQuery = true)
    List<RegistrationProjection> findUsersWithPatterns(@Param("courseId") Long courseId);
}
