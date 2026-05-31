package co.edu.uniremington.mscourses.dto;

public class CourseDto {
    private String name;
    private int credits;
    private int availableQuotas;

    public CourseDto() {}

    public CourseDto(String name, int credits, int availableQuotas) {
        this.name = name;
        this.credits = credits;
        this.availableQuotas = availableQuotas;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getAvailableQuotas() { return availableQuotas; }
    public void setAvailableQuotas(int availableQuotas) { this.availableQuotas = availableQuotas; }
}
