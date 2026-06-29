package be.ucll.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import be.ucll.dto.RegistrationDTOV2;
import be.ucll.dto.RegistrationProjection;
import be.ucll.model.*;
import be.ucll.repository.RegistrationDTOV2Repository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.ucll.repository.EventRepository;
import be.ucll.repository.RegistrationRepository;
import be.ucll.repository.StudentRepository;
import jakarta.transaction.Transactional;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final RegistrationDTOV2Repository registrationDTOV2Repository;
    // use StudentRepository to check STUDENT_IN_GROUP membership

    public RegistrationService(RegistrationRepository registrationRepository, StudentRepository studentRepository, EventRepository eventRepository, EventService eventService, RegistrationDTOV2Repository registrationDTOV2Repository) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.registrationDTOV2Repository = registrationDTOV2Repository;
    }

    public List<RegistrationDTOV2> getAllRegistrationsV2(Long eventId) {
        List<RegistrationDTOV2> registrationDTOV2s = registrationDTOV2Repository.findByEvent_Id(eventId);

        return registrationDTOV2s;
    }

    public List<RegistrationProjection> filterRegistrationsV2ByCourseIdAndPatterns(Long courseId) {
        return registrationDTOV2Repository.findUsersWithPatterns(courseId);
    }

    public HashMap<String, RegistrationDTOV2> findRegistrationV2sOfEvent(Long eventId) {
        List<RegistrationDTOV2> registrationDTOV2s = registrationDTOV2Repository.findByEvent_Id(eventId);
        HashMap<String, RegistrationDTOV2> registrationData = new HashMap<>();

        for (RegistrationDTOV2 registrationDTOV2 : registrationDTOV2s) {
            registrationData.put(registrationDTOV2.getStudentNumber(), registrationDTOV2);
        }

        return registrationData;
    }

    @Transactional
    public void processExcel(MultipartFile file, String type, Long eventId) {
        List<Registration> registrations = new ArrayList<>();

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip title row (first row)
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            // Read header row (second row) to get column mappings
            Map<String, Integer> columnMapping = new HashMap<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();

                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null) {
                        String originalColumnName = getCellValueAsString(headerCell).trim();
                        String columnName = originalColumnName.toLowerCase();
                        if (!columnName.isEmpty()) {
                            columnMapping.put(columnName, i);
                        }
                    }
                }

                if (columnMapping.isEmpty()) {
                    workbook.close();
                    throw new RuntimeException("No column headers found in the Excel file. Please ensure the second row contains column headers.");
                }
            } else {
                workbook.close();
                throw new RuntimeException("Excel file must have at least 2 rows (title and headers)");
            }

            RegistrationType registrationType = parseRegistrationType(type);

            int rowNumber = 3; // Starting from row 3 (after title and headers)
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Check if the row is empty (stop processing if we hit an empty row)
                if (isRowEmpty(row)) {
                    break;
                }

                Registration registration = processRow(row, registrationType, rowNumber, columnMapping, event);

                    if (registration != null) {
                        registrations.add(registration);
                    }
                rowNumber++;
            }

            // Save registrations with de-duplication rules:
            // - For check-in types (Checkin, LunchCheckin): keep the earliest timestamp
            // - For check-out types (Checkout, LunchCheckout): keep the latest timestamp
            if (!registrations.isEmpty()) {
                HashMap<String, RegistrationDTOV2> existingRegistrations = findRegistrationV2sOfEvent(eventId);

                for (Registration incoming : registrations) {
                    if (incoming.getStudent() == null || incoming.getStudent().getStudentNumber() == null || incoming.getType() == null) {
                        // Defensive: skip malformed entries
                        continue;
                    }

                    // Calling registrationRepository here because as of the moment before,
                    // registration is just a JSON object passed onto the backend.
                    // This JSON object does NOT yet contain the auto-generated ID;
                    // thus, we're making a call to JPA so that it gives us an ID that we can use.
                    Registration existingV1 = registrationRepository.findFirstByEvent_IdAndStudent_StudentNumberAndType(eventId, incoming.getStudentNumber(), registrationType);
                    if (existingV1  != null) {
                        incoming.setId(existingV1.getId());
                    } else {
                        incoming = registrationRepository.save(incoming);
                    }

                    boolean regExists = existingRegistrations.containsKey(incoming.getStudentNumber());
                    RegistrationDTOV2 existingV2 = null;
                    if (regExists) {
                       existingV2 = existingRegistrations.get(incoming.getStudentNumber());
                    }

                    if (regExists
                        && existingRegistrations.get(incoming.getStudentNumber()).getStatus() != StatusType.Unexpected) {
                        existingV2.setRegistrationData(incoming);
                        registrationDTOV2Repository.save(existingV2);
                    } else {
                        // New registration, save it

                        if (!regExists) {
                            // same reason as a bit above for line incoming.setId(id)
                            incoming = registrationRepository.save(incoming);
                            if (registrationType.equals(RegistrationType.Checkin)) {
                                registrationDTOV2Repository.save(new RegistrationDTOV2(incoming, null, event));
                            } else {
                                registrationDTOV2Repository.save(new RegistrationDTOV2(null, incoming, event));
                            }
                        } else {
                            existingV2.setRegistrationData(incoming);
                            registrationDTOV2Repository.save(existingV2);
                        }

                    }
                }
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }

    private Registration processRow(Row row, RegistrationType type, int rowNumber, Map<String, Integer> columnMapping, Event event) {
        try {
            // Get column indices from mapping - try multiple variations
            Integer usernameCol = getColumnIndex(columnMapping, "username", "user", "name", "user name");
            Integer firstnameCol = getColumnIndex(columnMapping, "firstname", "first name", "first_name", "fname", "given name");
            Integer lastnameCol = getColumnIndex(columnMapping, "lastname", "last name", "last_name", "lname", "surname", "family name");
            Integer emailCol = getColumnIndex(columnMapping, "email", "e-mail", "mail", "email address", "e_mail");
            Integer timestampCol = getColumnIndex(columnMapping, "timestamp", "time", "date", "datetime", "created", "submission time", "created at");

            // Check if email column exists (it's required)
            if (emailCol == null) {
                return null;
            }

            String username = "";
            String firstname = "";
            String lastname = "";
            String email = "";
            LocalDateTime timestampStr = LocalDateTime.now();

            if (usernameCol != null) {
                Cell usernameCell = row.getCell(usernameCol);
                username = getCellValueAsString(usernameCell);
            }

            if (firstnameCol != null) {
                Cell firstnameCell = row.getCell(firstnameCol);
                firstname = getCellValueAsString(firstnameCell);
            }

            if (lastnameCol != null) {
                Cell lastnameCell = row.getCell(lastnameCol);
                lastname = getCellValueAsString(lastnameCell);
            }

            Cell emailCell = row.getCell(emailCol);
            email = getCellValueAsString(emailCell);

            if (timestampCol != null) {
                Cell timestampCell = row.getCell(timestampCol);
                timestampStr = getCellValueAsLocalDateTime(timestampCell);
            }

            if (email == null || email.trim().isEmpty()) {
                return null;
            }

            String studentFirstName = !firstname.isEmpty() ? firstname :
                                    (!username.isEmpty() ? username :
                                    email.substring(0, email.indexOf('@')));
            String studentLastName = !lastname.isEmpty() ? lastname : "Unknown";

            // Prefer an explicit student number column if present (many upload templates include it)
            Integer studentNumberCol = getColumnIndex(columnMapping, "studentnumber", "inlognummer", "student number", "student_number");
            String studentNumber = null;
            if (studentNumberCol != null) {
                Cell snCell = row.getCell(studentNumberCol);
                studentNumber = getCellValueAsString(snCell).trim();
                System.out.println("  StudentNumber cell at index " + studentNumberCol + ": '" + studentNumber + "'");
            } else if (username != null) {
                // Some exports put the student number in the username column
                studentNumber = username.trim();
                System.out.println("  Using username as studentNumber: '" + studentNumber + "'");
            } else {
                // Fall back to generating a student number from email (r + hash)
                studentNumber = generateStudentNumber(email);
                System.out.println("  Generated studentNumber from email: '" + studentNumber + "'");
            }

            // Try to find existing student by student number
            Student student = studentRepository.findByStudentNumber(studentNumber);

            if (student == null) {
                System.out.println("Student not found for email: " + email + " in row " + rowNumber + ", creating new student");

                student = new Student(studentFirstName, studentLastName, studentNumber);

                student = studentRepository.save(student);
                System.out.println("Created new student: " + student.getFirstName() + " " + student.getLastName() + " with student number: " + student.getStudentNumber());
            }

            System.out.println("Successfully processed row " + rowNumber + " for student: " + student.getFirstName() + " " + student.getLastName());
            // Determine attended value based on STUDENT_IN_GROUP table
            if (isStudentInGroup(student.getStudentNumber())){
                Registration registration = new Registration(timestampStr, type, event, student, StatusType.Present);
            
                registration.setStudentNumber(studentNumber);
                // Store a snapshot of the student's basic info on the registration row
                try {
                    registration.setFirstName(student.getFirstName());
                } catch (Exception ignored) {}
                try {
                    registration.setLastName(student.getLastName());
                } catch (Exception ignored) {}
                try {
                    registration.setEmail(email);
                } catch (Exception ignored) {}
                
                return registration;

            } else {
                System.out.println("Student with studentNumber=" + student.getStudentNumber() + " is not in any group for the event; marking as unexpected.");
                Registration registration = new Registration(timestampStr, type, event, student, StatusType.Unexpected);
            
                registration.setStudentNumber(studentNumber);
                // Store a snapshot of the student's basic info on the registration row
                try {
                    registration.setFirstName(student.getFirstName());
                } catch (Exception ignored) {}
                try {
                    registration.setLastName(student.getLastName());
                } catch (Exception ignored) {}
                try {
                    registration.setEmail(email);
                } catch (Exception ignored) {}
                
                return registration;
            }

        } catch (Exception e) {
            System.out.println("Error processing row " + rowNumber + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a row is empty (all cells are null, blank, or empty strings)
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false; // Found non-empty content
                }
            }
        }
        return true; // All cells are empty
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // For numeric values, check if they're whole numbers
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private RegistrationType parseRegistrationType(String type) {
        try {
            return RegistrationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            // Default to Checkin if type is not recognized
            return RegistrationType.Checkin;
        }
    }

    private boolean isCheckinType(RegistrationType type) {
        return type == RegistrationType.Checkin || type == RegistrationType.LunchCheckin;
    }

    private LocalDateTime getCellValueAsLocalDateTime(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            try {
                // Try the standard format first: 16/05/2025 17:07:08.473
                DateTimeFormatter standardFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
                return LocalDateTime.parse(value, standardFormat);
            } catch (DateTimeParseException e) {
                try {
                    // Try without milliseconds: 16/05/2025 17:07:08
                    DateTimeFormatter formatWithoutMillis = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    return LocalDateTime.parse(value, formatWithoutMillis);
                } catch (DateTimeParseException e2) {
                    try {
                        // Try ISO format as fallback
                        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (DateTimeParseException e3) {
                        // Try other common formats
                        String[] fallbackFormats = {
                            "yyyy-MM-dd HH:mm:ss.SSS",
                            "yyyy-MM-dd HH:mm:ss",
                            "MM/dd/yyyy HH:mm:ss.SSS",
                            "MM/dd/yyyy HH:mm:ss"
                        };

                        for (String format : fallbackFormats) {
                            try {
                                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
                            } catch (DateTimeParseException ignored) {
                                // Continue to next format
                            }
                        }

                        System.out.println("Could not parse LocalDateTime from string: " + value);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return all registrations (attendees) in a lightweight map representation.
     * This endpoint should not filter by present/absent — filtering is available
     * on the events filter endpoint (/events/{id}/filter).
     */
    public List<Map<String, String>> getAllRegistrations() {
        List<Registration> regs = registrationRepository.findAll();
        List<Map<String, String>> registrations = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        for (Registration r : regs) {
            Map<String, String> m = new HashMap<>();
            String rnumber = r.getStudentNumber();
            if ((rnumber == null || rnumber.isEmpty()) && r.getStudent() != null) {
                rnumber = r.getStudent().getStudentNumber();
            }
            m.put("rnumber", rnumber == null ? "" : rnumber);
            m.put("firstName", r.getFirstName() != null ? r.getFirstName() : (r.getStudent() != null ? r.getStudent().getFirstName() : ""));
            m.put("lastName", r.getLastName() != null ? r.getLastName() : (r.getStudent() != null ? r.getStudent().getLastName() : ""));
            m.put("email", r.getEmail() != null ? r.getEmail() : "");
            m.put("timestamp", r.getDate() != null ? r.getDate().format(fmt) : "");
            m.put("status", String.valueOf(r.getStatus()));
            registrations.add(m);
        }

        return registrations;
    }

    public Registration createRegistration(Registration registration) {
        // Ensure attended flag is set based on STUDENT_IN_GROUP membership before saving
        try {
            if (registration != null && registration.getStudent() != null) {
                String sn = registration.getStudentNumber();
                if (sn == null || sn.isEmpty()) {
                    sn = registration.getStudent().getStudentNumber();
                    registration.setStudentNumber(sn);
                }
                boolean attended = isStudentInGroup(sn);
                registration.setStatus(StatusType.Present);
                // if firstname/lastname/email are missing, try to populate from linked student
                try {
                    if ((registration.getFirstName() == null || registration.getFirstName().isEmpty()) && registration.getStudent() != null) {
                        registration.setFirstName(registration.getStudent().getFirstName());
                    }
                } catch (Exception ignored) {}
                try {
                    if ((registration.getLastName() == null || registration.getLastName().isEmpty()) && registration.getStudent() != null) {
                        registration.setLastName(registration.getStudent().getLastName());
                    }
                } catch (Exception ignored) {}
                // email may not be stored on Student; keep whatever is present on registration
            }
        } catch (Exception e) {
            System.out.println("Error while checking student group membership: " + e.getMessage());
        }
        return registrationRepository.save(registration);
    }

    /**
     * Check whether a student number exists in the STUDENT_IN_GROUP table.
     */
    private boolean isStudentInGroup(String studentNumber) {
        if (studentNumber == null || studentNumber.isEmpty()) {
            return false;
        }
        try {
            long count = studentRepository.countInStudentInGroupByStudentNumber(studentNumber);
            return count > 0;
        } catch (Exception e) {
            System.out.println("Error querying STUDENT_IN_GROUP for studentNumber=" + studentNumber + ": " + e.getMessage());
            return false;
        }
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
     * Generate a student number from email address
     * Format: r + 7 digits based on email hash
     */
    private String generateStudentNumber(String email) {
        // Generate a hash from the email and convert to 7 digits
        int hash = Math.abs(email.hashCode());
        String digits = String.format("%07d", hash % 10000000);
        return "r" + digits;
    }

    public List<Registration> filterRegistrationsByDate(List<Registration> registrationsToCheck,
                                                        boolean late, boolean absent) {
        List<Registration> result = new ArrayList<>();
        Map<String, List<Registration>> registrationsByStudent = groupRegistrationsByStudent(registrationsToCheck);
        
        for (Map.Entry<String, List<Registration>> entry : registrationsByStudent.entrySet()) {
            List<Registration> studentRegistrations = entry.getValue();
            Registration checkInReg = findCheckInRegistration(studentRegistrations);
            Registration checkOutReg = findCheckOutRegistration(studentRegistrations);
            
            boolean isAbsent = isStudentAbsent(checkInReg, checkOutReg);
            boolean isPresent = !isAbsent;
            boolean isLate = isPresent && isStudentLate(checkInReg, checkOutReg);
            
            addFilteredRegistration(result, checkInReg, checkOutReg, late, absent, isLate, isAbsent, isPresent);
        }
        
        return result;
    }

    private Map<String, List<Registration>> groupRegistrationsByStudent(List<Registration> registrations) {
        Map<String, List<Registration>> registrationsByStudent = new HashMap<>();
        for (Registration registration : registrations) {
            String studentNumber = registration.getStudentNumber();
            registrationsByStudent.computeIfAbsent(studentNumber, k -> new ArrayList<>()).add(registration);
        }
        return registrationsByStudent;
    }

    private Registration findCheckInRegistration(List<Registration> studentRegistrations) {
        for (Registration reg : studentRegistrations) {
            if (reg.getType() == RegistrationType.Checkin || reg.getType() == RegistrationType.LunchCheckin) {
                return reg;
            }
        }
        return null;
    }

    private Registration findCheckOutRegistration(List<Registration> studentRegistrations) {
        for (Registration reg : studentRegistrations) {
            if (reg.getType() == RegistrationType.Checkout || reg.getType() == RegistrationType.LunchCheckout) {
                return reg;
            }
        }
        return null;
    }

    private boolean hasValidDate(Registration registration) {
        return registration != null && 
               registration.getDate() != null && 
               registration.getDate().getYear() != 1970;
    }

    private boolean isStudentAbsent(Registration checkInReg, Registration checkOutReg) {
        return !hasValidDate(checkInReg) && !hasValidDate(checkOutReg);
    }

    private boolean isStudentLate(Registration checkInReg, Registration checkOutReg) {
        boolean checkInLate = checkInReg != null && 
                             checkInReg.getEvent().getCheckInTime() != null &&
                             checkInReg.getDate().isAfter(checkInReg.getEvent().getCheckInTime());
        
        boolean checkOutLate = checkOutReg != null && 
                              checkOutReg.getEvent().getCheckOutTime() != null &&
                              checkOutReg.getDate().isAfter(checkOutReg.getEvent().getCheckOutTime());
        
        return checkInLate || checkOutLate;
    }

    private void addFilteredRegistration(List<Registration> result, Registration checkInReg, 
                                        Registration checkOutReg, boolean late, boolean absent, 
                                        boolean isLate, boolean isAbsent, boolean isPresent) {
        if (late && isLate) {
            if (checkInReg != null) {
                result.add(checkInReg);
            }
        } else if (absent && isAbsent) {
            if (checkInReg != null) {
                result.add(checkInReg);
            } else if (checkOutReg != null) {
                result.add(checkOutReg);
            }
        } else if (!late && !absent && isPresent && !isLate) {
            if (checkInReg != null) {
                result.add(checkInReg);
            }
        }
    }

    public List<Registration> findAllRegistrationsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return registrationRepository.findByEvent_Id(eventId);
    }

    public List<Registration> filterRegistrationsByExpected(Long eventId, List<Registration> registrationsToCheck,
                                                            boolean expected) {
        Event event = eventRepository.findById(eventId).get();
        HashSet<String> studentNumbers = new HashSet<>();

        for (Group group : event.getGroups()) {
            for (Student student : group.getAssignedStudents()) {
                studentNumbers.add(student.getStudentNumber());
            }
        }

        List<Registration> filteredRegistrations = new ArrayList<>();

        for (Registration registration : registrationsToCheck) {
            if (studentNumbers.contains(registration.getStudentNumber()) && expected) {
                filteredRegistrations.add(registration);
            } else if (!studentNumbers.contains(registration.getStudentNumber()) && !expected) {
                filteredRegistrations.add(registration);
            }
        }
        return filteredRegistrations;
    }

    public List<Registration> filterRegistrations(Long id, String filter, String registrationType) {
        // Validate that the event exists
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with id: " + id);
        }

        List<Registration> registrationsToCheck;
        // null is a string in this case because when you pass null as a requestParam,
        // it gets concatenated with the rest of the URL and turns into string
        if (registrationType == null || registrationType.equals("null")) {
            registrationsToCheck = findAllRegistrationsByEventId(id);
        } else {
            try {
                RegistrationType type = RegistrationType.valueOf(registrationType);
                registrationsToCheck = eventService.getStudentsByRegistrationType(id, type);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid registration type: " + registrationType);
            }
        }

        if ("unexpected".equalsIgnoreCase(filter)) {
            return filterRegistrationsByExpected(id, registrationsToCheck, false);
        } else {
            registrationsToCheck = filterRegistrationsByExpected(id, registrationsToCheck, true);
        }

        // Filter based on the attended flag or late status
        if ("late".equalsIgnoreCase(filter)) {
            return filterRegistrationsByDate(registrationsToCheck, true, false);
        }
        if ("present".equalsIgnoreCase(filter)) {
            // Present = not late AND not absent (students with both CI and CO on time)
            return filterRegistrationsByDate(registrationsToCheck, false, false);
        }
        if ("absent".equalsIgnoreCase(filter)) {
            // Absent = missing one or both registrations
            return filterRegistrationsByDate(registrationsToCheck, false, true);
        }
        
        // null is a string in this case because when you pass null as a requestParam,
        // it gets concatenated with the rest of the URL and turns into string
        if (filter != null && !filter.equals("null")) {
            throw new RuntimeException("Illegal filter type: " + filter);
        }

        // No filter: return all registrations for this event
        return findAllRegistrationsByEventId(id);
    }
    @Transactional
    public void toggleStudentAbsenceStatus(Long registrationId, boolean mainId) {
        Optional<RegistrationDTOV2> registrationDTOV2;
        if (mainId) {
            registrationDTOV2 = registrationDTOV2Repository.findById(registrationId);
            registrationDTOV2.ifPresent(dtov2 -> {
                dtov2.setValidAbsence(!dtov2.isValidAbsence());
                registrationDTOV2Repository.save(dtov2);
            });
            return;
        }
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        registration.toggleValidAbsence();
        registrationRepository.save(registration);

        if (registration.getType() == RegistrationType.Checkin) {
            registrationDTOV2 = registrationDTOV2Repository.findByCheckInId(registrationId);
        } else {
            registrationDTOV2 = registrationDTOV2Repository.findByCheckOutId(registrationId);
        }
        registrationDTOV2.ifPresent(dtov2 -> {
            dtov2.setValidAbsence(registration.isValidAbsence());
            registrationDTOV2Repository.save(dtov2);
        });
    }

    @Transactional
    public void deleteRegistrationById(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        Optional<RegistrationDTOV2> registrationDTOV2;

        if (registration.getType() == RegistrationType.Checkin) {
            registrationDTOV2 = registrationDTOV2Repository.findByCheckInId(registrationId);
            registrationDTOV2.ifPresent(dtov2 -> dtov2.eraseRegistrationData(RegistrationType.Checkin));
        } else {
            registrationDTOV2 = registrationDTOV2Repository.findByCheckOutId(registrationId);
            registrationDTOV2.ifPresent(dtov2 -> dtov2.eraseRegistrationData(RegistrationType.Checkout));
        }

        registrationRepository.deleteById(registrationId);
        registrationDTOV2.ifPresent(registrationDTOV2Repository::save);
    }

    public String selfRegister(SelfRegistration selfRegistration) {
        Event event = eventRepository.findById(selfRegistration.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (selfRegistration.rotatingCode() == null) throw new RuntimeException("Self registration rotatingCode not found");

        boolean authorised = selfRegistration.rotatingCode().equals(event.getEventRotationCode());
        if (authorised) {
            // Prepare registration data.
            Student student = studentRepository.findById(selfRegistration.rNumber())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Add registration.
            this.addManualAttendee(
                    event.getId(),
                    student.getStudentNumber(),
                    student.getFirstName(),
                    student.getLastName(),
                    selfRegistration.registrationType().equals(RegistrationType.Checkin) ? LocalDateTime.now().plusHours(1) : null,
                    selfRegistration.registrationType().equals(RegistrationType.Checkout) ? LocalDateTime.now().plusHours(1) : null
                    );

            return "\"Success.\"";
        } else {
            throw new RuntimeException("Incorrect code.");
        }
    }

    public RegistrationDTOV2 addManualAttendee(Long eventId,
        String studentNumber,
        String firstName,
        String lastName,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime) {

    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

    if (studentNumber == null || !studentNumber.matches("\\w\\d{7}")) {
        throw new RuntimeException("Student number must start with a letter and have seven digit afterwards, e.g. r1234567.");
    }

    if (firstName == null || lastName == null) {
        throw new RuntimeException("First name and last name required.");
    }

    if (checkInTime == null && checkOutTime == null) {
        throw new RuntimeException("At least check-in or check-out must be provided.");
    }

    // IMPORTANT: Ensure the student exists
    Student student = studentRepository.findByStudentNumber(studentNumber);
    if (student == null) {
        student = new Student(firstName, lastName, studentNumber);
        student = studentRepository.save(student);
    }

    Registration regCheckIn = null;
    Registration regCheckOut = null;

    if (checkInTime != null) {
        Registration r = new Registration();

        // Try to find an already existing registration.
        Registration alreadyExistingRegistration = registrationRepository.findFirstByEvent_IdAndStudent_StudentNumberAndType(eventId, studentNumber, RegistrationType.Checkin);
        if (alreadyExistingRegistration != null) r.setId(alreadyExistingRegistration.getId());

        r.setEvent(event);
        r.setStudent(student);                // <<< REQUIRED
        r.setStudentNumber(studentNumber);
        r.setFirstName(firstName);
        r.setLastName(lastName);
        r.setDate(checkInTime);
        r.setType(RegistrationType.Checkin);

        if (!event.isStudentExpected(studentNumber)) {
            r.setStatus(StatusType.Unexpected);
        } else {
            r.setStatus(StatusType.Present);
        }

        regCheckIn = registrationRepository.save(r);
    }

    if (checkOutTime != null) {
        Registration r = new Registration();
        // Try to find an already existing registration.
        Registration alreadyExistingRegistration = registrationRepository.findFirstByEvent_IdAndStudent_StudentNumberAndType(eventId, studentNumber, RegistrationType.Checkout);
        if (alreadyExistingRegistration != null) r.setId(alreadyExistingRegistration.getId());

        r.setEvent(event);
        r.setStudent(student);                // <<< REQUIRED
        r.setStudentNumber(studentNumber);
        r.setFirstName(firstName);
        r.setLastName(lastName);
        r.setDate(checkOutTime);
        r.setType(RegistrationType.Checkout);

        if (!event.isStudentExpected(studentNumber)) {
            r.setStatus(StatusType.Unexpected);
        } else {
            r.setStatus(StatusType.Present);
        }

        regCheckOut = registrationRepository.save(r);
    }

    // Try to find an already existing registrationDTOV2.
    Optional<RegistrationDTOV2> existingRegistrationDTOV2 = registrationDTOV2Repository.findByEvent_IdAndStudentNumber(eventId, studentNumber);
    RegistrationDTOV2 registrationDTOV2 = new RegistrationDTOV2(regCheckIn, regCheckOut, event);

    if (existingRegistrationDTOV2.isPresent()) {
        registrationDTOV2.setId(existingRegistrationDTOV2.get().getId());
    }

    registrationDTOV2Repository.save(registrationDTOV2);

    return new RegistrationDTOV2(regCheckIn, regCheckOut, event);
}
}