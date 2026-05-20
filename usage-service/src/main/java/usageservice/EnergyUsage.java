package usageservice;

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

    @Column(name = "community_produced", nullable = false)
    private double communityProduced = 0.0;

    @Column(name = "community_used", nullable = false)
    private double communityUsed = 0.0;

    @Column(name = "grid_used", nullable = false)
    private double gridUsed = 0.0;

    public EnergyUsage() {}

    public EnergyUsage(LocalDateTime hour) {
        this.hour = hour;
    }

    public Long getId()                              { return id; }
    public LocalDateTime getHour()                   { return hour; }
    public void setHour(LocalDateTime hour)          { this.hour = hour; }
    public double getCommunityProduced()             { return communityProduced; }
    public void setCommunityProduced(double v)       { this.communityProduced = v; }
    public double getCommunityUsed()                 { return communityUsed; }
    public void setCommunityUsed(double v)           { this.communityUsed = v; }
    public double getGridUsed()                      { return gridUsed; }
    public void setGridUsed(double v)                { this.gridUsed = v; }
}
