package co.edu.uniremington.mscourses.controller;

import co.edu.uniremington.mscourses.exception.CourseNotFoundException;
import co.edu.uniremington.mscourses.exception.NoAvailableQuotasException;
import co.edu.uniremington.mscourses.model.Course;
import co.edu.uniremington.mscourses.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ── POST /api/courses ─────────────────────────────────────────────────────

    @Test
    void create_ShouldReturn200WithCreatedCourse() throws Exception {
        Course toCreate = new Course(null, "Física", 3, 20);
        Course created  = new Course(1L,   "Física", 3, 20);
        when(courseService.save(any(Course.class))).thenReturn(created);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Física"))
                .andExpect(jsonPath("$.credits").value(3));
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── DELETE /api/courses/{id} ──────────────────────────────────────────────

    @Test
    void delete_WhenCourseExists_ShouldReturn200() throws Exception {
        doNothing().when(courseService).delete(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).delete(99L);

        mockMvc.perform(delete("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/courses/{id}/decrease-quota ──────────────────────────────────

    @Test
    void decreaseQuota_WhenSuccess_ShouldReturn200() throws Exception {
        doNothing().when(courseService).decreaseQuota(1L);

        mockMvc.perform(put("/api/courses/1/decrease-quota"))
                .andExpect(status().isOk());
    }

    @Test
    void decreaseQuota_WhenNoQuotas_ShouldReturn409() throws Exception {
        doThrow(new NoAvailableQuotasException(1L)).when(courseService).decreaseQuota(1L);

        mockMvc.perform(put("/api/courses/1/decrease-quota"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void decreaseQuota_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).decreaseQuota(99L);

        mockMvc.perform(put("/api/courses/99/decrease-quota"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/courses/{id}/increase-quota ──────────────────────────────────

    @Test
    void increaseQuota_WhenSuccess_ShouldReturn200() throws Exception {
        doNothing().when(courseService).increaseQuota(1L);

        mockMvc.perform(put("/api/courses/1/increase-quota"))
                .andExpect(status().isOk());
    }

    @Test
    void increaseQuota_WhenCourseNotFound_ShouldReturn404() throws Exception {
        doThrow(new CourseNotFoundException(99L)).when(courseService).increaseQuota(99L);

        mockMvc.perform(put("/api/courses/99/increase-quota"))
                .andExpect(status().isNotFound());
    }
}
