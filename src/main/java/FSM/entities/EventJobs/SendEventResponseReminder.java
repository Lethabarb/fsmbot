package FSM.entities.EventJobs;

import java.time.ZonedDateTime;

import FSM.entities.Event;
import FSM.services.DiscordBot;
import FSM.services.EventJobRunner;

public class SendEventResponseReminder extends EventJob {

    public SendEventResponseReminder(Event e) {
        super(e, e.getDateTime().minusDays(1));
        EventJobRunner.getInstance().addJob(this);
    }

    @Override
    public void action() {
        event.sendResponseReminder();
    }
    @Override
    public String toString() {
        return event.getTitle() + " - send reminders @ " + timeToAction.toString();
    }

}
