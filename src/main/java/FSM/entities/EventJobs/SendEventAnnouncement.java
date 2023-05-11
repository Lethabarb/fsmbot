package FSM.entities.EventJobs;

import FSM.entities.Event;
import FSM.services.DiscordBot;
import FSM.services.EventJobRunner;

public class SendEventAnnouncement extends EventJob {

    public SendEventAnnouncement(Event e) {
        super(e, e.getDateTime().minusMinutes(30));
        EventJobRunner.getInstance().addJob(this);

    }

    @Override
    public void action() {
        event.sendAnnounceReminder();
    }
    @Override
    public String toString() {
        return event.getTitle() + " - send announcement @ " + timeToAction.toString();
    }
}
