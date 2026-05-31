package co.edu.uniremington.mscourses.controller;

import co.edu.uniremington.mscourses.dto.CourseDto;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.service.CourseService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> findAll() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public Course create(@RequestBody CourseDto dto) {
        return courseService.saveCourse(dto);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        return courseService.update(id, course);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }

    @PutMapping("/{id}/reserve-slot")
    public void reserveSlot(@PathVariable Long id) {
        courseService.reserveSlot(id);
    }

    @PutMapping("/{id}/release-slot")
    public void releaseSlot(@PathVariable Long id) {
        courseService.releaseSlot(id);
    }
}
