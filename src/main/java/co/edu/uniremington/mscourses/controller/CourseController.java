package co.edu.uniremington.mscourses.controller;

import co.edu.uniremington.mscourses.dto.CourseDto;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Courses", description = "Course management operations")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "List all courses", description = "Returns a list of all registered courses")
    @GetMapping
    public List<Course> findAll() {
        return courseService.findAll();
    }

    @Operation(summary = "Get course by ID", description = "Returns a single course by its ID")
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @Operation(summary = "Create a course", description = "Registers a new course with name, credits and available quotas")
    @PostMapping
    public ResponseEntity<Course> create(@RequestBody CourseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.saveCourse(dto));
    }

    @Operation(summary = "Update a course", description = "Updates the data of an existing course")
    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        return courseService.update(id, course);
    }

    @Operation(summary = "Delete a course", description = "Removes a course from the system by its ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }

    @Operation(summary = "Reserve a slot", description = "Decreases available quotas by 1. Called internally by ms-students when a student enrolls")
    @PutMapping("/{id}/reserve-slot")
    public void reserveSlot(@PathVariable Long id) {
        courseService.reserveSlot(id);
    }

    @Operation(summary = "Release a slot", description = "Increases available quotas by 1. Called internally by ms-students when an enrollment is cancelled")
    @PutMapping("/{id}/release-slot")
    public void releaseSlot(@PathVariable Long id) {
        courseService.releaseSlot(id);
    }
}
