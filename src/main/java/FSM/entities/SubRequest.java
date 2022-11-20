package FSM.entities;

import net.dv8tion.jda.api.entities.Message;

public class SubRequest {
    private Player player;
    private int subRole;
    private Message message;
    public SubRequest(Player p, Message m, int role) {
        this.player = p;
        this.subRole = role;
        this.message = m;
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
}
