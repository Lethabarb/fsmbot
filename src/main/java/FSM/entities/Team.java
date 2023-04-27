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
import okhttp3.internal.ws.RealWebSocket.Message;

public class Team implements Runnable {
    private static LinkedList<Team> teams = new LinkedList<>();
    private static boolean avail = true;
    private String name;
    private String nameAbbv;
    private String sheetPageName;
    private String minRank;
    private MessageChannel timetable;
    private MessageChannel announcement;
    private Server guild;
    private Role rosterRole;
    private Role trialRole;
    private Role subRole;
    private List<Member> members = new LinkedList<>();
    private int teamupSubCalendar;
    private boolean hasDuelSheetSetup = false;

    private GoogleSheet sheet;

    // public Team(String name, String nameAbbv, String minRank, MessageChannel
    // timetable, Role rosterRole, Role trialRole,
    // Role subRole, List<Member> members, int teamupSubCalendar) {
    // this.name = name;
    // this.nameAbbv = nameAbbv;
    // this.minRank = minRank;
    // this.timetable = timetable;
    // this.rosterRole = rosterRole;
    // this.trialRole = trialRole;
    // this.subRole = subRole;
    // this.members = members;
    // this.teamupSubCalendar = teamupSubCalendar;
    // this.sheet = new GoogleSheet();
    // Thread t = new Thread(this, name);
    // teams.add(this);
    // t.start();
    // }

    public Team(String name, String nameAbbv, String minRank, MessageChannel timetable, MessageChannel announcement, Role rosterRole, Role trialRole,
            Role subRole, List<Member> members, int teamupSubCalendar, String sheetId) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetable = timetable;
        this.announcement = announcement;
        this.rosterRole = rosterRole;
        this.trialRole = trialRole;
        this.subRole = subRole;
        this.members = members;
        this.teamupSubCalendar = teamupSubCalendar;
        this.sheet = new GoogleSheet(sheetId);
        Thread t = new Thread(this, name);
        teams.add(this);
        t.start();
    }

    public Team(String name, String nameAbbv, String minRank, MessageChannel timetable, Role rosterRole, Role trialRole,
            Role subRole, List<Member> members, int teamupSubCalendar, String sheetId, String sheetPageName) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetable = timetable;
        this.rosterRole = rosterRole;
        this.trialRole = trialRole;
        this.subRole = subRole;
        this.members = members;
        this.teamupSubCalendar = teamupSubCalendar;
        this.sheet = new GoogleSheet(sheetId);
        Thread t = new Thread(this, name);
        teams.add(this);
        t.start();
    }

    @Override
    public void run() {
        DiscordBot bot = DiscordBot.getInstance();
        Boolean first = true;
        while (true) {
            int c = 0;
            while (!avail) {
                try {
                    Thread.sleep(1000);
                    System.out.println(name + " is waiting to update..." + c++);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            try {
                System.out.println("==========" + name + "==========");
                avail = false;
                try {
                    if (first) {
                        bot.createEventsFromChanel(timetable, this);
                    }
                    first = false;
                    bot.updateScrims(this);
                    bot.sortChannel(timetable);
                } catch (Exception e) {
                    // TODO: handle exception
                } finally {
                    avail = true;
                }
                Thread.sleep(12 * 60 * 60 * 1000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean hasTrials() {
        DiscordBot bot = DiscordBot.getInstance();
        List<Member> mems = bot.getMemberOfRole(guild.getGuild(), trialRole);
        return mems.size() > 0;
    }

    public static boolean getAvailability() {
        return avail;
    }
    // public synchronized static void updateScrims(Team t) {
    // System.out.println("updating scrims for " + t.getName());
    // DiscordBot bot = DiscordBot.getInstance();
    // try {
    // GoogleSheet sheet = new GoogleSheet();
    // LinkedList<Event> events = sheet.getEvents(t.getNameAbbv(), t);
    // for (int i = 0; i < events.size(); i++) {
    // bot.sendEvent(events.get(i), true);
    // }
    // // updateAllEvents(t);
    // MessageChannel c = t.getTimetable();
    // bot.sortChannel(c);
    // System.out.println("done updating scrims");
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

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

    public int getTeamupSubCalendar() {
        return teamupSubCalendar;
    }

    public void setTeamupSubCalendar(int teamupSubCalendar) {
        this.teamupSubCalendar = teamupSubCalendar;
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

    public static Team getTeamByRosterRole(Role r) {
        for (Team team : teams) {
            if (team.getRosterRole().compareTo(r) == 0) {
                return team;
            }
        }
        return teams.getFirst();
    }

    public static Team getTeamByName(String name) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(name)) return team;
        }
        return null;
    }

    public static LinkedList<Team> getTeams() {
        return teams;
    }

    public static void setTeams(LinkedList<Team> teams) {
        Team.teams = teams;
    }

    public static boolean isAvail() {
        return avail;
    }

    public static void setAvail(boolean avail) {
        Team.avail = avail;
    }

    public String getSheetPageName() {
        return sheetPageName;
    }

    public void setSheetPageName(String sheetPageName) {
        this.sheetPageName = sheetPageName;
    }

    public Server getGuild() {
        return guild;
    }

    public void setGuild(Server guild) {
        this.guild = guild;
    }

    public boolean isHasDuelSheetSetup() {
        return hasDuelSheetSetup;
    }

    public void setHasDuelSheetSetup(boolean hasDuelSheetSetup) {
        this.hasDuelSheetSetup = hasDuelSheetSetup;
    }

    public MessageChannel getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(MessageChannel announcement) {
        this.announcement = announcement;
    }
}
