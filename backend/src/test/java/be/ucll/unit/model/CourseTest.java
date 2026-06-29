package be.ucll.unit.model;

import be.ucll.model.Course;
import be.ucll.model.Group;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CourseTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void createValidator() {
        validatorFactory = buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void givenValidValues_whenCreatingCourse_thenCourseIsCreated() {
        Course course = new Course("A course", "A course description");

        assertEquals("A course", course.getName());
        assertEquals("A course description", course.getDescription());
    }

    @Test
    public void givenNullName_whenCreatingCourse_thenARuntimeErrorIsThrown() {
        Course course = new Course(null, "A course description");

        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Course name is required.", violations.iterator().next().getMessage());
    }

    @Test
    public void givenEmptyName_whenCreatingCourse_thenARuntimeErrorIsThrown() {
        Course course = new Course("    ", "A course description");

        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Course name is required.", violations.iterator().next().getMessage());
    }

    @Test
    public void givenNoDescription_whenCreatingCourse_thenACourseIsCreatedWithName() {
        Course course = new Course("A course");

        assertEquals("A course", course.getName());
    }
}
