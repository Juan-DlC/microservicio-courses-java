package co.edu.uniremington.mscourses.service;

import co.edu.uniremington.mscourses.dto.CourseDto;
import co.edu.uniremington.mscourses.exception.CourseNotFoundException;
import co.edu.uniremington.mscourses.exception.NoSlotsAvailableException;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    public Course saveCourse(CourseDto dto) {
        Course course = new Course(null, dto.getName(), dto.getCredits(), dto.getAvailableQuotas());
        return courseRepository.save(course);
    }

    public Course update(Long id, Course courseDetails) {
        return courseRepository.findById(id).map(existingCourse -> {
            existingCourse.setName(courseDetails.getName());
            existingCourse.setCredits(courseDetails.getCredits());
            return courseRepository.save(existingCourse);
        }).orElseThrow(() -> new CourseNotFoundException(id));
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        courseRepository.deleteById(id);
    }

    public void reserveSlot(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getAvailableQuotas() <= 0) {
            throw new NoSlotsAvailableException(courseId);
        }

        course.setAvailableQuotas(course.getAvailableQuotas() - 1);
        courseRepository.save(course);
    }

    public void releaseSlot(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        course.setAvailableQuotas(course.getAvailableQuotas() + 1);
        courseRepository.save(course);
    }
}
