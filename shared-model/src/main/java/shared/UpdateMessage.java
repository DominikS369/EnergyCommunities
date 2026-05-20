package shared;

import java.time.LocalDateTime;

public class UpdateMessage {

    private LocalDateTime hour;

    public UpdateMessage() {}

    public UpdateMessage(LocalDateTime hour) {
        this.hour = hour;
    }

    public LocalDateTime getHour()           { return hour; }
    public void setHour(LocalDateTime hour)  { this.hour = hour; }
}
