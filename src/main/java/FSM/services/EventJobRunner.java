package FSM.services;

import java.time.ZonedDateTime;
import java.util.PriorityQueue;
import java.util.TimeZone;

import FSM.entities.Comparators.EventJobComparator;
import FSM.entities.EventJobs.EventJob;

public class EventJobRunner implements Runnable{
    private Thread t;
    private PriorityQueue<EventJob> jobs = new PriorityQueue<>(new EventJobComparator());
    private static EventJobRunner instance = null;

    private EventJobRunner() {
        t = new Thread(this, "EventJobRunner");
        t.start();
    }
    public static EventJobRunner getInstance() {
        if (instance == null) {
            instance = new EventJobRunner();
        }
        return instance;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        while (true) {
            long waitTime = 100;
            long jobTime = jobs.peek().getdt().toEpochSecond();
            long nowTime = ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId()).toEpochSecond();
            waitTime = jobTime - nowTime;
            System.out.println("=============");
            // while (!this.jobs.isEmpty()) {
            //     System.out.println(jobs.poll().toString());
            // }
            if (jobs.peek() == null || nowTime < jobTime) {
                System.out.println(jobs.peek().toString());
                if (waitTime < 0) waitTime = 100;
                try {
                    Thread.sleep(waitTime*1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                System.out.println("[EventJobRunner]: actioning");
                jobs.poll().action();
            }
        }
    }
    public void addJob(EventJob eJob) {
        System.out.println(eJob.toString());
        jobs.add(eJob);
    }
}
