package percentageservice;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_usage")
public class EnergyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime hour;
    private double communityProduced;
    private double communityUsed;
    private double gridUsed;

    public LocalDateTime getHour()          { return hour; }
    public double getCommunityProduced()    { return communityProduced; }
    public double getCommunityUsed()        { return communityUsed; }
    public double getGridUsed()             { return gridUsed; }
}