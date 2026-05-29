package co.edu.uniremington.mscourses.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int availableQuotas;

    // Constructor vacío exigido por JPA
    public Course() {
    }

    // Constructor con parámetros
    public Course(Long id, String name, int credits, int availableQuotas) {
        this.id = id;
        this.name = name;
        this.credits = credits;
        this.availableQuotas = availableQuotas;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getAvailableQuotas()
    {
        return availableQuotas;
    }
    public void setAvailableQuotas(int availableQuotas)
    {
        this.availableQuotas = availableQuotas;
    }
}