package timertest;

import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author pete
 */
@ApplicationScoped
public class AppConfig {

    private String schedule;

    public AppConfig() {
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

}
