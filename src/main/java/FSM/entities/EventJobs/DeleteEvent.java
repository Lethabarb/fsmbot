package FSM.entities.EventJobs;

import java.io.IOException;

import FSM.entities.Event;
import FSM.services.EventJobRunner;

public class DeleteEvent extends EventJob {

    public DeleteEvent(Event e) {
        super(e, e.getDateTime().plusHours(3));
        EventJobRunner.getInstance().addJob(this);

    }

    @Override
    public void action() {
        try {
            event.deleteEvent("Scrim has passed, thanks for playing!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return event.getTitle() + " - delete @ " + timeToAction.toString();
    }
}
