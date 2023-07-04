package FSM.entities;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.JdkConstants.PatternFlags;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Player {
    private static HashMap<String, Player> repository = new HashMap<>();
    public static final int TANK = 0;
    public static final int DPS = 1;
    public static final int SUPPORT = 2;
    private String name;
    private Member member;
    private int role;
    private String userId;
    public String at;
    
    public Player(Member m, int role) {
        member = m;
        this.name = m.getUser().getName();
        this.role = role;
        this.userId = m.getUser().getId();
        at = m.getAsMention();
        repository.put(name, this);
        // System.out.println(name + " is new");
    }
    public Player(Member m) {
        member = m;
        this.name = m.getUser().getName();
        this.userId = m.getUser().getName();
        at = m.getAsMention();
        int i = 0;
        int r = -1;
        while (i < m.getRoles().size() && r == -1) {
            r = roleHash(m.getRoles().get(i).getName());
            i++;
        }
        this.role = role;
    }
    
    
    public static Player getPlayer(Member m) {
        return repository.getOrDefault(m.getUser().getName(), null);
    }
    public static Player getPlayerByName(String name) {
        return repository.getOrDefault(name, null);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getRole() {
        return role;
    }
    
    public void setRole(int role) {
        this.role = role;
    }
    public String getUserId() {
        return userId;
    }
    
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    

    public static int roleHash(String role) {


        //andromeda
        if (role.equalsIgnoreCase("offtank")) return Player.TANK;
        if (role.equalsIgnoreCase("maintank")) return Player.TANK;
        if (role.equalsIgnoreCase("hitscan dps")) return Player.DPS;
        if (role.equalsIgnoreCase("projectile dps")) return Player.DPS;
        if (role.equalsIgnoreCase("mainsupport")) return Player.SUPPORT;
        if (role.equalsIgnoreCase("aimsupport")) return Player.SUPPORT;

        //fsm / ambition
        if (role.equalsIgnoreCase("tank")) return Player.TANK;
        if (role.equalsIgnoreCase("support")) return Player.SUPPORT;
        if (role.equalsIgnoreCase("dps")) return Player.DPS;
        if (role.equalsIgnoreCase("damage")) return Player.DPS;
        
        return -1;
    }

}
