package FSM.entities;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import FSM.services.DiscordBot;
import FSM.services.GoogleSheet;
import io.opencensus.trace.Link;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class Team implements Runnable {
    private String name;
    private String nameAbbv;
    private String minRank;
    private MessageChannel timetable;
    private Server guild;
    private Role rosterRole;
    private Role trialRole;
    private Role subRole;
    private List<Member> members = new LinkedList<>();
    
    private GoogleSheet sheet = new GoogleSheet();
    
    public Team(String name, String nameAbbv, String minRank, MessageChannel timetable, Role rosterRole, Role trialRole,
    Role subRole, List<Member> members) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetable = timetable;
        this.rosterRole = rosterRole;
        this.trialRole = trialRole;
        this.subRole = subRole;
        this.members = members;
        Thread t = new Thread(this, name);
        t.start();
    }
    
    @Override
    public void run() {
        DiscordBot bot = DiscordBot.getInstance();
        bot.createEventsFromChanel(timetable, this);
        while (true) {
            try {
                bot.updateScrims(this);
                Thread.sleep(12*60*60*100);;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNameAbbv() {
        return nameAbbv;
    }
    
    public void setNameAbbv(String nameAbbv) {
        this.nameAbbv = nameAbbv;
    }
    
    public String getMinRank() {
        return minRank;
    }
    
    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
    public void setMinRank(String minRank) {
        this.minRank = minRank;
    }

    public MessageChannel getTimetable() {
        return timetable;
    }

    public void setTimetable(MessageChannel timetable) {
        this.timetable = timetable;
    }

    public Role getRosterRole() {
        return rosterRole;
    }

    public void setRosterRole(Role rosterRole) {
        this.rosterRole = rosterRole;
    }

    public Role getSubRole() {
        return subRole;
    }

    public void setSubRole(Role subRole) {
        this.subRole = subRole;
    }

    public GoogleSheet getSheet() {
        return sheet;
    }

    public void setSheet(GoogleSheet sheet) {
        this.sheet = sheet;
    }

    public Server getServer() {
        return guild;
    }

    public void setServer(Server s) {
        this.guild = s;
    }

    public Role getTrialRole() {
        return trialRole;
    }

    public void setTrialRole(Role trialRole) {
        this.trialRole = trialRole;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nameAbbv == null) ? 0 : nameAbbv.hashCode());
        result = prime * result + ((minRank == null) ? 0 : minRank.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Team other = (Team) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nameAbbv == null) {
            if (other.nameAbbv != null)
                return false;
        } else if (!nameAbbv.equals(other.nameAbbv))
            return false;
        if (minRank == null) {
            if (other.minRank != null)
                return false;
        } else if (!minRank.equals(other.minRank))
            return false;
        return true;
    }

}
