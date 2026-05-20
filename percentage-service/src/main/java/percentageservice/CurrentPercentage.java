package percentageservice;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "current_percentage")
public class CurrentPercentage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDateTime hour;

    private double communityDepleted;
    private double gridPortion;

    public CurrentPercentage() {}

    public CurrentPercentage(LocalDateTime hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public LocalDateTime getHour()              { return hour; }
    public void setHour(LocalDateTime hour)     { this.hour = hour; }
    public double getCommunityDepleted()        { return communityDepleted; }
    public void setCommunityDepleted(double v)  { this.communityDepleted = v; }
    public double getGridPortion()              { return gridPortion; }
    public void setGridPortion(double v)        { this.gridPortion = v; }
}