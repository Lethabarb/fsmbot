package FSM.entities.Comparators;

import FSM.entities.EventJobs.EventJob;
import java.util.Comparator;

public class EventJobComparator implements Comparator<EventJob>{

    public EventJobComparator() {}

    @Override
    public int compare(EventJob o1, EventJob o2) {
        return o1.getdt().compareTo(o2.getdt());
    }

}