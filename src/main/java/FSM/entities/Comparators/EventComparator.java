package FSM.entities.Comparators;

import java.util.Comparator;

import FSM.entities.Event;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event o1, Event o2) {
        //negitive if o1 is sooner
        int res = Long.compare(o1.getUnix(), o2.getUnix());
        if (res == 0) {
            res = o1.getTitle().compareTo(o2.getTitle());
        }
        return res;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'compare'");
    }
    
}
