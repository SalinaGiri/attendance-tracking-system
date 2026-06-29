package be.ucll.unit.service;

import be.ucll.model.Course;
import be.ucll.model.Group;
import be.ucll.repository.CourseRepository;
import be.ucll.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    public void givenDefaultRepository_whenCreatingService_thenRepositoryIsFilledWithCourseData() {
        List<Course> testCourses =new ArrayList<>(List.of(
                new Course("IT & Society", "Society & IT, duhh"),
                new Course("Full-Stack Development", "Front-End and Back-End, joined together"),
                new Course("Server & System Management", "Pain... eternal pain"),
                new Course("Data & Machine Learning", "Math, some more math and finally, meth")
        ));

        Mockito.when(courseRepository.findAll()).thenReturn(testCourses);

        assertEquals(testCourses.size(), courseService.getAllCourses().size());
        assertIterableEquals(testCourses, courseService.getAllCourses(), "Courses should match exactly.");
    }

    @Test
    public void givenMixedCourses_whenCreatingService_thenRepositoryIsFilledWithCourseData() {
        List<Course> testCourses =new ArrayList<>(List.of(
                new Course("IT & Society"),
                new Course("Full-Stack Development", "Front-End and Back-End, joined together"),
                new Course("Server & System Management"),
                new Course("Data & Machine Learning", "Math, some more math and finally, meth")
        ));

        Mockito.when(courseRepository.findAll()).thenReturn(testCourses);

        assertEquals(testCourses.size(), courseService.getAllCourses().size());
        assertIterableEquals(testCourses, courseService.getAllCourses(), "Courses should match exactly.");
    }
}
