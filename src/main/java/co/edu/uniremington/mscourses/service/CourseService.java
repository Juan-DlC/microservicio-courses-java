package co.edu.uniremington.mscourses.service;

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
        }).orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + id));
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    public void decreaseQuota(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

        if (course.getAvailableQuotas() <= 0) {
            throw new RuntimeException("No available quotas for this course");
        }

        course.setAvailableQuotas(course.getAvailableQuotas() - 1);
        courseRepository.save(course);
    }

    public void increaseQuota(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

        course.setAvailableQuotas(course.getAvailableQuotas() + 1);
        courseRepository.save(course);
    }
}