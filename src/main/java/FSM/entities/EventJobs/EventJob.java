package FSM.entities.EventJobs;

import java.time.ZonedDateTime;
import java.util.Comparator;

import FSM.entities.Event;

public abstract class EventJob {
    protected Event event;
    protected ZonedDateTime timeToAction;

    public EventJob(Event e, ZonedDateTime timeToAction) {
        this.event = e;
        this.timeToAction = timeToAction;
    }
    public EventJob() {}
    
    public ZonedDateTime getdt() {
        return timeToAction;
    }
    public String getEventTitle() {
        return event.getTitle();
    }
    public String toString() {
        return getEventTitle();
    }
    public void action() {}
}
