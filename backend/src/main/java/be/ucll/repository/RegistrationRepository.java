package be.ucll.repository;

import java.time.LocalDateTime;
import java.util.List;

import be.ucll.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import be.ucll.model.Registration;
import be.ucll.model.RegistrationType;
import be.ucll.model.StatusType;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEvent_Id(Long eventId);

    List<Registration> findByEvent_IdAndType(Long eventId, RegistrationType type);

    List<Registration> findByEvent_IdAndStatus(Long eventId, StatusType status);

    List<Registration> findByEvent_IdAndStatusAndDateIsNull(Long eventId, StatusType status);

    Registration findFirstByEvent_IdAndStudent_StudentNumber(Long eventId, String studentNumber);

    Registration findFirstByEvent_IdAndStudent_StudentNumberAndType(Long eventId, String studentNumber, RegistrationType type);
}



