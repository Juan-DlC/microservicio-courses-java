package co.edu.uniremington.mscourses.service;

import co.edu.uniremington.mscourses.exception.CourseNotFoundException;
import co.edu.uniremington.mscourses.exception.NoAvailableQuotasException;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    // ── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_ShouldReturnAllCourses() {
        List<Course> courses = List.of(
                new Course(1L, "Cálculo", 4, 30),
                new Course(2L, "Álgebra", 3, 25)
        );
        when(courseRepository.findAll()).thenReturn(courses);

        List<Course> result = courseService.findAll();

        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }

    // ── getCourseById ────────────────────────────────────────────────────────

    @Test
    void getCourseById_WhenCourseExists_ShouldReturnCourse() {
        Course course = new Course(1L, "Cálculo", 4, 30);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Course result = courseService.getCourseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cálculo", result.getName());
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void getCourseById_WhenCourseNotFound_ShouldThrowCourseNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> courseService.getCourseById(99L));
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_ShouldPersistAndReturnCourse() {
        Course course = new Course(null, "Física", 3, 20);
        Course saved  = new Course(1L,   "Física", 3, 20);
        when(courseRepository.save(any(Course.class))).thenReturn(saved);

        Course result = courseService.save(course);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_WhenCourseExists_ShouldUpdateAndReturn() {
        Course existing = new Course(1L, "Cálculo Viejo", 3, 30);
        Course details  = new Course(null, "Cálculo Nuevo", 4, 30);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenReturn(existing);

        Course result = courseService.update(1L, details);

        assertEquals("Cálculo Nuevo", result.getName());
        assertEquals(4, result.getCredits());
        verify(courseRepository, times(1)).save(existing);
    }

    @Test
    void update_WhenCourseNotFound_ShouldThrowCourseNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> courseService.update(99L, new Course()));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_WhenCourseExists_ShouldCallDeleteById() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        courseService.delete(1L);

        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WhenCourseNotFound_ShouldThrowCourseNotFoundException() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThrows(CourseNotFoundException.class, () -> courseService.delete(99L));
        verify(courseRepository, never()).deleteById(any());
    }

    // ── decreaseQuota ─────────────────────────────────────────────────────────

    @Test
    void decreaseQuota_WhenQuotasAvailable_ShouldDecrementByOne() {
        Course course = new Course(1L, "Cálculo", 4, 5);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        courseService.decreaseQuota(1L);

        assertEquals(4, course.getAvailableQuotas());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void decreaseQuota_WhenNoQuotasLeft_ShouldThrowNoAvailableQuotasException() {
        Course course = new Course(1L, "Cálculo", 4, 0);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThrows(NoAvailableQuotasException.class,
                () -> courseService.decreaseQuota(1L));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void decreaseQuota_WhenCourseNotFound_ShouldThrowCourseNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> courseService.decreaseQuota(99L));
    }

    // ── increaseQuota ─────────────────────────────────────────────────────────

    @Test
    void increaseQuota_WhenCourseExists_ShouldIncrementByOne() {
        Course course = new Course(1L, "Cálculo", 4, 4);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        courseService.increaseQuota(1L);

        assertEquals(5, course.getAvailableQuotas());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void increaseQuota_WhenCourseNotFound_ShouldThrowCourseNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> courseService.increaseQuota(99L));
    }
}
