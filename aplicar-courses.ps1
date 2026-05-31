# ============================================================
# Script PowerShell - microservicio-courses-java
# Ejecutar desde PowerShell dentro de la carpeta del repo:
#   cd C:\Proyectos\Proyecto_Microservicios\microservicio-courses-java
#   .\aplicar-courses.ps1
# ============================================================

$repoPath = Get-Location
Write-Host "Aplicando cambios en: $repoPath" -ForegroundColor Cyan

Write-Host "  Creando CourseNotFoundException.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception" | Out-Null
Set-Content -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception\CourseNotFoundException.java" -Value @'
package co.edu.uniremington.mscourses.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(Long id) {
        super("Curso no encontrado con ID: " + id);
    }
}

'@ -NoNewline

Write-Host "  Creando NoAvailableQuotasException.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception" | Out-Null
Set-Content -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception\NoAvailableQuotasException.java" -Value @'
package co.edu.uniremington.mscourses.exception;

public class NoAvailableQuotasException extends RuntimeException {
    public NoAvailableQuotasException(Long courseId) {
        super("No hay cupos disponibles para el curso con ID: " + courseId);
    }
}

'@ -NoNewline

Write-Host "  Creando GlobalExceptionHandler.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception" | Out-Null
Set-Content -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\exception\GlobalExceptionHandler.java" -Value @'
package co.edu.uniremington.mscourses.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCourseNotFound(CourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoAvailableQuotasException.class)
    public ResponseEntity<Map<String, String>> handleNoQuotas(NoAvailableQuotasException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}

'@ -NoNewline

Write-Host "  Creando CourseService.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\service" | Out-Null
Set-Content -Path "$repoPath\src\main\java\co\edu\uniremington\mscourses\service\CourseService.java" -Value @'
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

'@ -NoNewline

Write-Host "  Creando CourseServiceTest.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\test\java\co\edu\uniremington\mscourses\service" | Out-Null
Set-Content -Path "$repoPath\src\test\java\co\edu\uniremington\mscourses\service\CourseServiceTest.java" -Value @'
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

'@ -NoNewline

Write-Host "  Creando CourseControllerTest.java..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\test\java\co\edu\uniremington\mscourses\controller" | Out-Null
Set-Content -Path "$repoPath\src\test\java\co\edu\uniremington\mscourses\controller\CourseControllerTest.java" -Value @'
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

'@ -NoNewline

Write-Host "  Creando application.properties..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$repoPath\src\test\resources" | Out-Null
Set-Content -Path "$repoPath\src\test\resources\application.properties" -Value @'
spring.datasource.url=jdbc:h2:mem:testcoursesdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.h2.console.enabled=false

eureka.client.enabled=false
spring.cloud.discovery.enabled=false
spring.cloud.service-registry.auto-registration.enabled=false

'@ -NoNewline

Write-Host "  Creando pom.xml..." -ForegroundColor Yellow
Set-Content -Path "$repoPath\pom.xml" -Value @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>
    <groupId>co.edu.uniremington</groupId>
    <artifactId>ms-courses</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>ms-courses</name>
    <description>ms-courses</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.1.1</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-h2console</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.5.0</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- JACOCO: cobertura mínima 80% -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.12</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>verify</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                    <execution>
                        <id>check</id>
                        <goals><goal>check</goal></goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.80</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                            <excludes>
                                <exclude>**/model/**</exclude>
                                <exclude>**/dto/**</exclude>
                                <exclude>**/*Application*</exclude>
                                <exclude>**/repository/**</exclude>
                            </excludes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

'@ -NoNewline

Write-Host ""
Write-Host "Haciendo commit..." -ForegroundColor Cyan
git add .
git commit -m "feat: add unit tests JUnit+Mockito, Jacoco 80%, GlobalExceptionHandler"
Write-Host ""
Write-Host "Listo! Ahora ejecuta: git push origin master" -ForegroundColor Green
Write-Host "Para verificar cobertura:  mvn clean verify" -ForegroundColor Green