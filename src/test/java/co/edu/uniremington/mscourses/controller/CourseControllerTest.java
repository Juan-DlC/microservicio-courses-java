package co.edu.uniremington.mscourses.controller;

import co.edu.uniremington.mscourses.dto.CourseDto;
import co.edu.uniremington.mscourses.exception.CourseNotFoundException;
import co.edu.uniremington.mscourses.exception.GlobalExceptionHandler;
import co.edu.uniremington.mscourses.exception.NoSlotsAvailableException;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(courseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /api/courses ──────────────────────────────────────────────────────

    @Test
    void findAll_ShouldReturn200WithList() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(
                new Course(1L, "Cálculo", 4, 30),
                new Course(2L, "Álgebra", 3, 25)
        ));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cálculo"));
    }

    // ── GET /api/courses/{id} ─────────────────────────────────────────────────

    @Test
    void getCourseById_WhenCourseExists_ShouldReturn200() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(
                new Course(1L, "Cálculo", 4, 30));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cálculo"));
    }

    @Test
    void getCourseById_WhenCourseNotFound_ShouldReturn404() throws Exception {
        when(courseService.getCourseById(99L))
                .thenThrow(new CourseNotFoundException(99L));

        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/courses ─────────────────────────────────────────────────────

    @Test
    void create_ShouldReturn200WithCreatedCourse() throws Exception {
        CourseDto dto   = new CourseDto("Física", 3, 20);
        Course created  = new Course(1L, "Física", 3, 20);
        when(courseService.saveCourse(any(CourseDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Física"));
    }

    // ── PUT /api/courses/{id} ─────────────────────────────────────────────────

    @Test
    void update_WhenCourseExists_ShouldReturn200() throws Exception {
        Course updated = new Course(1L, "Cálculo Avanzado", 5, 30);
        when(courseService.update(eq(1L), any(Course.class))).thenReturn(updated);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cálculo Avanzado"));
    }

    @Test
    void update_WhenCourseNotFound_ShouldReturn404() throws Exception {
        when(courseService.update(eq(99L), any(Course.class)))
                .thenThrow(new CourseNotFoundException(99L));

        mockMvc.perform(put("/api/courses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Course())))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/courses/{id} ──────────────────────────────────────────────

    @Test
    void delete_WhenCourseExists_ShouldReturn200() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).deleteCourse(99L);

        mockMvc.perform(delete("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/courses/{id}/reserve-slot ────────────────────────────────────

    @Test
    void reserveSlot_WhenSuccess_ShouldReturn200() throws Exception {
        doNothing().when(courseService).reserveSlot(1L);

        mockMvc.perform(put("/api/courses/1/reserve-slot"))
                .andExpect(status().isOk());
    }

    @Test
    void reserveSlot_WhenNoSlots_ShouldReturn409() throws Exception {
        doThrow(new NoSlotsAvailableException(1L)).when(courseService).reserveSlot(1L);

        mockMvc.perform(put("/api/courses/1/reserve-slot"))
                .andExpect(status().isConflict());
    }

    @Test
    void reserveSlot_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).reserveSlot(99L);

        mockMvc.perform(put("/api/courses/99/reserve-slot"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/courses/{id}/release-slot ────────────────────────────────────

    @Test
    void releaseSlot_WhenSuccess_ShouldReturn200() throws Exception {
        doNothing().when(courseService).releaseSlot(1L);

        mockMvc.perform(put("/api/courses/1/release-slot"))
                .andExpect(status().isOk());
    }

    @Test
    void releaseSlot_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).releaseSlot(99L);

        mockMvc.perform(put("/api/courses/99/release-slot"))
                .andExpect(status().isNotFound());
    }
}
