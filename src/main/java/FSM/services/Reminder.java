package FSM.services;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.PriorityQueue;
import java.util.TimeZone;

import FSM.entities.Event;
import FSM.entities.Comparators.EventComparator;

public class Reminder implements Runnable {
    private static Reminder instance = null;
    private PriorityQueue<Event> queue = new PriorityQueue<>(new EventComparator());

    private Reminder() {}

    public static Reminder getInstance() {
        if (instance == null) instance = new Reminder();
        return instance;
    }

    public void addToQueue(Event e) {
        if (!queue.contains(e)) queue.add(e);
    }

    @Override
    public void run() {
        ZonedDateTime dt = ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
        boolean isBefore = true;
        while (isBefore) {
            if (queue.peek().getDateTime().minusMinutes(30).compareTo(dt) < 0) {
                Event e = queue.poll();
                e.sendReminder();
            }
        }

        try {
            Thread.sleep(6000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
    
}
