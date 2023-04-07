package FSM.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.sound.midi.SysexMessage;

import FSM.services.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class Event implements Comparable<Event> {
    //test
    public static final int SCRIM = 0;
    public static final int AAOL = 1;
    public static final int COACHING = 2;
    public static final int OPENDIV = 3;

    private static HashMap<Long, Event> repository = new HashMap<>();
    private String title;
    private LocalDateTime dateTime;
    private Message message;
    private String contact1; // discord
    private String contact2; // bnet
    private Team team;
    private int type;
    private boolean sentAnnouncement = false;

    private LinkedList<Player> notResponded = new LinkedList<>();
    private LinkedList<Player> confimed = new LinkedList<>();
    private LinkedList<Player> declined = new LinkedList<>();
    private LinkedList<SubRequest> subs = new LinkedList<>();

    // i love you

    public Event(String title, LocalDateTime dateTime, Message message, String contact1, String contact2, Team team,
            int type) {
        this.title = title;
        this.dateTime = dateTime;
        this.message = message;
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.team = team;
        this.type = type;
        List<Member> members = DiscordBot.getInstance().getMemberOfRole(team.getServer().getGuild(),
                team.getRosterRole(), team.getTrialRole());
        for (Member member : members) {
            // System.out.println("========" + member.getUser().getName() + "========");
            if (Player.getPlayer(member) == null) {
                int OWrole = -1;
                for (Role role : member.getRoles()) {
                    if (OWrole == -1) {
                        OWrole = Player.roleHash(role.getName());
                    }
                }
                Player p = new Player(member, OWrole);
                notResponded.add(p);
            } else {
                notResponded.add(Player.getPlayer(member));
            }
        }
        if (repository.get(gethashCode()) == null) {
            System.out.println(title + " is new");
            repository.put(gethashCode(), this);
        }
        // System.out.println(gethashCode());
        // team.getServer().addEventChoice(this);
    }

    public long getUnix() {
        Long unix = dateTime.toEpochSecond(ZoneId.of("Australia/Sydney").getRules().getOffset(LocalDateTime.now()));
        return unix;
    }

    public long gethashCode() {
        return getUnix() + hashCode();
    }

    public boolean hasFullRoster() {
        int dpsCount = 0;
        int tankCount = 0;
        int supportCount = 0;

        for (Player player : confimed) {
            if (player.getRole() == Player.DPS)
                dpsCount++;
            if (player.getRole() == Player.TANK)
                tankCount++;
            if (player.getRole() == Player.SUPPORT)
                supportCount++;
        }
        for (int i = 0; i < 5; i++) {
            if (subs.get(i) != null && subs.get(i).getPlayer() != null) {
                if (subs.get(i).getSubRole() == Player.TANK)
                    tankCount++;
                if (subs.get(i).getSubRole() == Player.DPS)
                    dpsCount++;
                if (subs.get(i).getSubRole() == Player.SUPPORT)
                    supportCount++;
            }
        }
        if (tankCount >= 1 && dpsCount >= 2 && supportCount >= 2) {
            return true;
        }
        return false;
    }

    public void updateScrim() {
        DiscordBot bot = DiscordBot.getInstance();
        System.out.println(String.format("scrim id: ", message.getId()));
        bot.updateEvent(this);
    }

    @Override
    public int compareTo(Event o) {
        return dateTime.compareTo(o.getDateTime());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getContact1() {
        return contact1;
    }

    public void setContact1(String contact1) {
        this.contact1 = contact1;
    }

    public String getContact2() {
        return contact2;
    }

    public void setContact2(String contact2) {
        this.contact2 = contact2;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean isSentAnnouncement() {
        return sentAnnouncement;
    }

    public void setSentAnnouncement(boolean sentAnnouncement) {
        this.sentAnnouncement = sentAnnouncement;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean removeFromLists(Player player) {
        notResponded.remove(player);
        confimed.remove(player);
        boolean wasDeclined = declined.remove(player);
        return wasDeclined;
    }

    public boolean addConfirmed(Player player) {
        boolean sub = removeFromLists(player);
        confimed.add(player);
        return sub;
    }

    public String confirmed() {
        String s = "";
        String[] roles = { "tank", "dps", "support" };
        for (Player player : confimed) {
            s += player.at + " - " + roles[player.getRole()] + "\n";
        }
        return s;
    }
    public String confirmedString() {
        String s = "";
        for (Player p : confimed) {
            s += p.getName() + " ";
        }
        return s;
    }
    public String notResponded() {
        String s = "";
        for (Player player : notResponded) {
            s += player.at + "\n";
        }
        return s;
    }
    public String notRespondedString() {
        String s = "";
        for (Player p : notResponded) {
            s += p.getName() + " ";
        }
        return s;
    }

    public void deleteAllSubs() {
        while (!subs.isEmpty()) {
            SubRequest sub = subs.getFirst();
            try {
                team.getServer().getGuild().removeRoleFromMember(sub.getPlayer().getMember(), team.getSubRole());
                sub.getMessage().delete().queue();
            } catch (Exception e) {
                System.out.println("no sub message, deleting sub");
            }
            subs.remove(sub);
        }
    }
    public String declined() {
        String s = "";
        for (Player player : declined) {
            s += player.at + "\n";
        }
        return s;
    }
    public String declinedString() {
        String s = "";
        for (Player p : declined) {
            s += p.getName() + " ";
        }
        return s;
    }
    public boolean confirmedContains(Player p) {
        return confimed.contains(p);
    }
    public boolean declinedContains(Player p) {
        return declined.contains(p);
    }

    public void addDeclined(Player player) {
        removeFromLists(player);
        declined.add(player);
    }


    public void addNR(Player player) {
        removeFromLists(player);
        notResponded.add(player);
    }


    public static Event getEvent(long key) {
        return repository.get(key);
    }
    public static Event addEvent(long key, Event e) {
        return repository.put(key,e);
    }

    public void addSub(SubRequest sub) {
        subs.add(sub);

        // TODO: plan sub shit cuz its complicated af
        /*
         * player declines
         * sub req sent
         * sub accepts req
         * del req
         * add to sub ll
         * sub press dec -> add check
         * resend req
         * remove from ll
         * sub press accept -> add check
         * do nothing
         */
    }

    public boolean needsSub(int role) {
        int count = 0;

        for (Player player : confimed) {
            if (player.getRole() == role)
                count++;
        }
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i) != null && subs.get(i).getPlayer() != null) {
                if (subs.get(i).getSubRole() == role)
                    count++;
            }
        }
        if (role == Player.TANK && count >= 1)
            return false;
        if (role == Player.DPS && count >= 2)
            return false;
        if (role == Player.SUPPORT && count >= 2)
            return false;
        return true;
    }

    // public int getSubIndex(int role) {
    // // subs.get(i)
    // if (role == Player.TANK) {
    // return 0 + tankSubCount;
    // } else if (role == Player.DPS) {
    // return 1 + dpsSubCount;
    // } else if (role == Player.SUPPORT) {
    // return 3 + supportSubCount;
    // }
    // return -1;
    // }
    public int getSubIndex() {
        return subs.size();
    }

    public int getExistingSub(int role, boolean isFilled) {
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i).getSubRole() == role) {
                if (isFilled) {
                    if (subs.get(i).getPlayer() != null)
                        return i;
                } else {
                    return i;
                }
            }
            ;
        }
        return -1;
    }

    public void removeSub(int i) {
        subs.remove(subs.get(i));
    }

    public Message getSubMessage(int i) {
        return subs.get(i).getMessage();
    }

    public void setSubPlayer(int i, Player p) {
        subs.get(i).setPlayer(p);
    }

    // public int subHash(int role) {
    // if (subs[role*2] == null) {
    // return role*2;
    // } else if (subs[role*2+1] == null) {
    // return role*2+1;
    // }
    // return -1;
    // }

    public String subs() {
        String res = " ";
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i).getPlayer() != null) {
                res += subs.get(i).getPlayer().at;
                if (subs.get(i).getSubRole() == Player.TANK) {
                    res += " - Tank\n";
                }
                if (subs.get(i).getSubRole() == Player.DPS) {
                    res += " - DPS\n";
                }
                if (subs.get(i).getSubRole() == Player.SUPPORT) {
                    res += " - Support\n";
                }
            }
        }
        return res;
    }

    public LinkedList<Player> getNotResponded() {
        return notResponded;
    }

    public static void removeFromRepository(Event e) {
        repository.remove(e.gethashCode());
    }

    public void setNotResponded(LinkedList<Player> notResponded) {
        this.notResponded = notResponded;
    }

    // public LinkedList<Player> getConfimed() {
    // return confimed;
    // }

    // public void setConfimed(LinkedList<Player> confimed) {
    // this.confimed = confimed;
    // }

    // public LinkedList<Player> getDeclined() {
    // return declined;
    // }

    // public void setDeclined(LinkedList<Player> declined) {
    // this.declined = declined;
    // }

    // public HashMap<Message, Player> getSubs() {
    // return subs;
    // }

    // public void setSubs(HashMap<Message, Player> subs) {
    // this.subs = subs;
    // }

    public static LinkedList<Event> getAllEvents() {
        return new LinkedList<>(repository.values());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        // result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
        // result = prime * result + ((contact1 == null) ? 0 : contact1.hashCode());
        // result = prime * result + ((contact2 == null) ? 0 : contact2.hashCode());
        result = prime * result + ((team == null) ? 0 : team.hashCode());
        result = prime * result + type;
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
        Event other = (Event) obj;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (dateTime == null) {
            if (other.dateTime != null)
                return false;
        } else if (!dateTime.equals(other.dateTime))
            return false;
        if (contact1 == null) {
            if (other.contact1 != null)
                return false;
        } else if (!contact1.equals(other.contact1))
            return false;
        if (contact2 == null) {
            if (other.contact2 != null)
                return false;
        } else if (!contact2.equals(other.contact2))
            return false;
        if (team == null) {
            if (other.team != null)
                return false;
        } else if (!team.equals(other.team))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public static ArrayList<Event> messagesToEvents(List<Message> messages) {
        ArrayList<Event> res = new ArrayList<>();

        for (Message m : messages) {
            try {
                Event e = repository.get(Long.parseLong(m.getEmbeds().get(0).getFooter().getText()));
                if (e != null) res.add(e);
            } catch (Exception ex) {
                // TODO: handle exception
            }
        }

        return res;
    }
}