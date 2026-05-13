package restapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_usage")
public class EnergyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDateTime hour;

    @Column(nullable = false)
    private Double communityProduced = 0.0;

    @Column(nullable = false)
    private Double communityUsed = 0.0;

    @Column(nullable = false)
    private Double gridUsed = 0.0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getHour() { return hour; }
    public void setHour(LocalDateTime hour) { this.hour = hour; }

    public Double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(Double communityProduced) { this.communityProduced = communityProduced; }

    public Double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(Double communityUsed) { this.communityUsed = communityUsed; }

    public Double getGridUsed() { return gridUsed; }
    public void setGridUsed(Double gridUsed) { this.gridUsed = gridUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

