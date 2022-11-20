package FSM.entities;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.JdkConstants.PatternFlags;

import net.dv8tion.jda.api.entities.Member;

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
        System.out.println(name + " is new");
    }
    
    
    
    public static Player getPlayer(Member m) {
        return repository.get(m.getUser().getName());
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
    // public static void main(String[] args) {
        // System.out.println(roleHash("Hitscan dps"));
        // System.out.println(roleHash("maintank"));
        // System.out.println(roleHash("main tank"));
        // System.out.println(roleHash("offtank"));
        // System.out.println(roleHash("off tank"));
        // System.out.println(roleHash("mt"));
        // System.out.println(roleHash("ot"));
        // System.out.println(roleHash("damage"));
        // System.out.println(roleHash("dps"));
        // System.out.println(roleHash("proj"));
        // System.out.println(roleHash("projectile"));
        // System.out.println(roleHash("proj dps"));
        // System.out.println(roleHash("projectile dps"));
        // System.out.println(roleHash("projdps"));
        // System.out.println(roleHash("projectiledps"));
        // System.out.println(roleHash("hs"));
        // System.out.println(roleHash("hitscan"));
        // System.out.println(roleHash("hit scan"));
        // System.out.println(roleHash("hit-scan"));
        // System.out.println(roleHash("hs dps"));
        // System.out.println(roleHash("hitscan dps"));
        // System.out.println(roleHash("hit scan dps"));
        // System.out.println(roleHash("hit-scan dps"));
        // System.out.println(roleHash("hsdps"));
        // System.out.println(roleHash("hitscandps"));
        // System.out.println(roleHash("hit scandps"));
        // System.out.println(roleHash("hit-scandps"));
        // System.out.println(roleHash("as"));
        // System.out.println(roleHash("aimsupp"));
        // System.out.println(roleHash("main support"));


    // }

    public static int roleHash(String role) {
        // String dpsRegex = "(hs|proj)|(dps)|(hit)|(damage)";
        // String tankRegex = "(m|o)t|tank";
        // String supportRegex = "(a|m(ain)?) ?s|supp(ort)?";
        // System.out.println(role);
        // Pattern dpsPattern = Pattern.compile(dpsRegex, Pattern.CASE_INSENSITIVE);
        // Pattern tankPattern = Pattern.compile(tankRegex, Pattern.CASE_INSENSITIVE);
        // Pattern suppPattern = Pattern.compile(supportRegex, Pattern.CASE_INSENSITIVE);
        // Matcher dps = dpsPattern.matcher(role);
        // Matcher tank = tankPattern.matcher(role);
        // Matcher supp = suppPattern.matcher(role);
        // if (dps.find()) return Player.DPS;
        // if (tank.find()) return Player.TANK;
        // if (supp.find()) return Player.SUPPORT;

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
        
        return -1;
    }

}
