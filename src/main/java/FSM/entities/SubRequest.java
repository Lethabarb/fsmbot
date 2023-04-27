package FSM.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.dv8tion.jda.api.entities.Message;

public class SubRequest {
    private static HashMap<String, SubRequest> repos = new HashMap<>();
    private String uuid;
    private Player player;
    private int subRole;
    private Message message;
    private Event event;
    public SubRequest(Player p, Message m, int role, Event e) {
        this.player = p;
        this.subRole = role;
        this.message = m;
        event = e;
        uuid = UUID.randomUUID().toString();
        repos.put(uuid, this);
    }
    public SubRequest(Player p, Message m, int role, long e) {
        this.player = p;
        this.subRole = role;
        this.message = m;
        event = Event.getEvent(e);
        uuid = UUID.randomUUID().toString();
        repos.put(uuid, this);
    }
    public SubRequest(String UUID, Event e, int role) {
        
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player p) {
        this.player = p;
    }
    public Message getMessage() {
        return message;
    }
    public void setMessage(Message m) {
        this.message = m;
    }
    public int getSubRole() {
        return subRole;
    }
    public void setSubRole(int subRole) {
        this.subRole = subRole;
    }
    public static void deleteSubRequestsForEvent(Event e) {
        LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
        for (SubRequest req : requests) {
            if (req.getEvent().compareTo(e) == 0) {
                try {
                    req.message.delete().complete();
                    
                } catch (Exception ex) {
                    System.out.println("couldnt del scrim, continueing...");
                }
            }
        }
    }
    public static SubRequest getRequest(String UUID) {
        return repos.get(UUID);
    }
    public static SubRequest getRequestByRole(Event e, int role, boolean filled) {
        Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0 && req.getSubRole() != role);
        if (filled) pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0 && req.getSubRole() != role && req.getPlayer() != null);
        LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
        requests.removeIf(pred);
        return requests.getFirst();
    }
    public static SubRequest getRequestByPlayer(Event e, Player p) {
        Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0 && req.getPlayer().equals(p));
        LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
        requests.removeIf(pred);
        return requests.getFirst();
    }
    public static LinkedList<SubRequest> getRequestForEvent(Event e) {
        Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0);
        LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
        requests.removeIf(pred);
        return requests;
    }
    public boolean deleteRequest() {
        return repos.remove(uuid, this);
    }
    public static String getSubString(Event e) {
        System.out.println("called getSubString for event " + e.getTitle());
        String res = " ";
        Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0);
        LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
        System.out.println(requests.size());
        requests.removeIf(pred);
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getPlayer() != null) {
                res += requests.get(i).getPlayer().at;
                if (requests.get(i).getSubRole() == Player.TANK) {
                    res += " - Tank\n";
                }
                if (requests.get(i).getSubRole() == Player.DPS) {
                    res += " - DPS\n";
                }
                if (requests.get(i).getSubRole() == Player.SUPPORT) {
                    res += " - Support\n";
                }
            }
        }
        System.out.println(res);
        return res;
    }
    public static HashMap<String, SubRequest> getRepos() {
        return repos;
    }
    public static void setRepos(HashMap<String, SubRequest> repos) {
        SubRequest.repos = repos;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
}
