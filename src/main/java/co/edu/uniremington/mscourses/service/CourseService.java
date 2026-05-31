package co.edu.uniremington.mscourses.service;

import co.edu.uniremington.mscourses.exception.CourseNotFoundException;
import co.edu.uniremington.mscourses.exception.NoAvailableQuotasException;
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

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public Course update(Long id, Course courseDetails) {
        return courseRepository.findById(id).map(existingCourse -> {
            existingCourse.setName(courseDetails.getName());
            existingCourse.setCredits(courseDetails.getCredits());
            return courseRepository.save(existingCourse);
        }).orElseThrow(() -> new CourseNotFoundException(id));
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        courseRepository.deleteById(id);
    }

    public void decreaseQuota(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        if (course.getAvailableQuotas() <= 0) {
            throw new NoAvailableQuotasException(id);
        }

        course.setAvailableQuotas(course.getAvailableQuotas() - 1);
        courseRepository.save(course);
    }

    public void increaseQuota(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        course.setAvailableQuotas(course.getAvailableQuotas() + 1);
        courseRepository.save(course);
    }
}
