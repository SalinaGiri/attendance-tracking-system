package be.ucll.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ucll.dto.RegistrationDTO;
import be.ucll.dto.RegistrationDTOV2;
import be.ucll.dto.RegistrationProjection;
import be.ucll.model.Event;
import be.ucll.model.SelfRegistration;
import be.ucll.service.FileValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.ucll.model.Registration;
import be.ucll.service.RegistrationService;

@RestController
@RequestMapping("/registrations")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class RegistrationController {
    private RegistrationService registrationService;
    private FileValidationService fileValidationService;

    public RegistrationController(RegistrationService registrationService,
                                  FileValidationService fileValidationService) {
        this.registrationService = registrationService;
        this.fileValidationService = fileValidationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRegistrations(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("eventId") Long eventId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            fileValidationService.validateExcelFile(file);

            // Process the Excel file
            registrationService.processExcel(file, type, eventId);

            response.put("message", "Registrations uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public List<Map<String, String>> getAllRegistrations() {
        return registrationService.getAllRegistrations();
    }

    @GetMapping("/v2/{eventId}/all")
    public ResponseEntity<List<RegistrationDTOV2>> getAllRegistrationsV2(@PathVariable Long eventId) {
        return new ResponseEntity<>(registrationService.getAllRegistrationsV2(eventId), HttpStatus.OK);
    }

    @GetMapping("/v2/{courseId}")
    public ResponseEntity<List<RegistrationProjection>> getRegistrationsV2WithPatternsByCourseId(@PathVariable Long courseId) {
        return new ResponseEntity<>(registrationService.filterRegistrationsV2ByCourseIdAndPatterns(courseId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Registration> createRegistration(@RequestBody Registration registration) {
        Registration createdRegistration = registrationService.createRegistration(registration);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRegistration);
    }

    @GetMapping("/{id}/filter")
    public List<RegistrationDTO> filterRegistrations(
            @PathVariable Long id,
            @RequestParam(required = false, name = "filter") String filter,
            @RequestParam(required = false, name = "registration-type") String registrationType
    ) {
        System.out.println("Filter registartions method, registration-type: " + registrationType);
        return registrationService.filterRegistrations(id, filter, registrationType)
                .stream()
                .map(RegistrationDTO::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @PatchMapping("/{id}/absence")
    public ResponseEntity<Void> updateRegistrationAbsence(
            @PathVariable("id") Long registrationId, 
            @RequestParam(value = "mainId", required = false, defaultValue = "false") boolean mainId) {
        registrationService.toggleStudentAbsenceStatus(registrationId, mainId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable("id") Long registrationId) {
        registrationService.deleteRegistrationById(registrationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/self-registration")
    public ResponseEntity<String> selfRegister(@RequestBody SelfRegistration selfRegistration) {
        String response = registrationService.selfRegister(selfRegistration);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{eventId}/manual")
public ResponseEntity<?> addManualAttendee(
        @PathVariable Long eventId,
        @RequestBody Map<String, Object> input) {  // use Map to get raw JSON

            String studentNumber = (String) input.getOrDefault("studentNumber", null);
            String firstName = (String) input.getOrDefault("firstName", null);
            String lastName = (String) input.getOrDefault("lastName", null);            

    LocalDateTime checkInTime = input.get("checkInTime") != null ?
            LocalDateTime.parse((String) input.get("checkInTime")) : null;

    LocalDateTime checkOutTime = input.get("checkOutTime") != null ?
            LocalDateTime.parse((String) input.get("checkOutTime")) : null;

    RegistrationDTOV2 result = registrationService.addManualAttendee(
            eventId, studentNumber, firstName, lastName, checkInTime, checkOutTime);

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}





    


}
