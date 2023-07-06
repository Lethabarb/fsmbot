package FSM.entities;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.poi.sl.draw.geom.Guide;
import org.apache.poi.ss.usermodel.DataFormat;

import FSM.entities.EventJobs.DeleteEvent;
import FSM.entities.EventJobs.SendEventAnnouncement;
import FSM.entities.EventJobs.SendEventResponseReminder;
import FSM.services.DiscordBot;
import FSM.services.GoogleSheet;
import FSM.services.GoogleSheet2;
import kotlin.collections.builders.ListBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class Event extends ListenerAdapter implements Comparable<Event> {
    // test
    public static final int SCRIM = 0;
    public static final int AAOL = 1;
    public static final int COACHING = 2;
    public static final int OPENDIV = 3;
    public static final String[] typeStrings = { "Scrim", "AAOL", "Coaching", "Open Div" };

    private static HashMap<Long, Event> repository = new HashMap<>();
    private String title;
    private ZonedDateTime dateTime;
    private Message message = null;
    private String disc; // discord
    private String bnet; // bnet
    private Team team;
    private int type;
    private boolean inqueue = false;
    // private boolean sentAnnouncement = false;
    // private boolean sentReminders = false;
    private MessageCreateData messageData;

    private LinkedList<Player> notResponded = new LinkedList<>();
    private LinkedList<Player> confimed = new LinkedList<>();
    private LinkedList<Player> declined = new LinkedList<>();
    private LinkedList<SubRequest> subRequests = new LinkedList<>();
    // private SubRequest[] subs = new SubRequest[5];

    // i love you

    public Event(String title, ZonedDateTime dateTime, Message message, String contact1, String contact2, Team team,
            int type) {
        this.title = title;
        this.dateTime = dateTime;
        // System.out.println(dateTime.toString());
        this.message = message;
        this.disc = contact1;
        this.bnet = contact2;
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
    }

    public Event(String title, ZonedDateTime dateTime, String contact1, String contact2, Team team,
            int type) {
        this.title = title;
        this.dateTime = dateTime;
        this.disc = contact1;
        this.bnet = contact2;
        this.team = team;
        this.type = type;
    }

    public void createJobs() {
        DeleteEvent delEvent = new DeleteEvent(this);
        SendEventAnnouncement announceEvent = new SendEventAnnouncement(this);
        SendEventResponseReminder responseEvent = new SendEventResponseReminder(this);
    }

    public long getUnix() {
        // Long unix =
        // dateTime.toEpochSecond(ZoneId.of("Australia/Sydney").getRules().getOffset(LocalDateTime.now()));
        Long unix = dateTime.toEpochSecond();
        return unix;
    }

    public long gethashCode() {
        return getUnix() + hashCode();
    }

    public boolean isSub(Player p) {
        // LinkedList<SubRequest> requests = SubRequest.getRequestForEvent(this);
        for (SubRequest subRequest : subRequests) {
            try {
                if (subRequest.getPlayer().getUserId().equalsIgnoreCase(p.getUserId()))
                    return true;
            } catch (Exception e) {
                System.out.println("sub req has no player");
            }
        }
        return false;
    }

    public void sendAnnounceReminder() {
        String roleAts = team.getRosterRole().getAsMention();
        if (team.hasTrials())
            roleAts += " " + team.getTrialRole().getAsMention();
        String content = roleAts + " Scrim in <t:" + getUnix() + ":R>.";
        team.getAnnouncement().sendMessage(content).queue((res) -> {
            System.out.println("sent announcement for " + team.getName());
        });
    }

    public void sendResponseReminder() {
        for (Player p : notResponded) {
            Member member = p.getMember();
            User user = member.getUser();
            user.openPrivateChannel().complete().sendMessage("reminder to respond to scrim on <t:" + getUnix() + ":F>")
                    .queue((res) -> {
                        System.out.println("sent reminder to " + user.getName());
                    });
            ;
        }
    }

    public void deleteEvent(String reason) throws IOException {
        System.out.println("called delete event");
        message.delete().queue(
                (res) -> {
                    message = null;
                    System.out.println("deleted message");
                }, (res) -> {
                    message = null;
                    System.out.println("could not delete message");
                });

        // List<SubRequest> reqs = SubRequest.getRequestForEvent(this);
        for (SubRequest req : subRequests) {
            req.delete(reason);
        }
        GoogleSheet2 sheet = new GoogleSheet2();
        sheet.deleteEvent(this);
        repository.remove(gethashCode());
    }

    public void updateRoster() {
        // Guild g = team.getServer().getGuild();
        System.out.println(String.format("[%s]: current roster roles - %s + %s", team.getName(), team.getRosterRole(),
                team.getTrialRole()));
        List<Member> members = DiscordBot.getInstance().getMemberOfRole(team.getServer().getGuild(),
                team.getTrialRole(), team.getRosterRole());
        LinkedList<Player> players = new LinkedList<>();
        for (Member m : members) {
            System.out.println(String.format("[]: instance", m.getUser().getName()));
            Player p = Player.getPlayer(m);
            if (p == null) {
                System.out.println(String.format("[]: is new", m.getUser().getName()));
                int OWrole = -1;
                for (Role role : m.getRoles()) {
                    if (OWrole == -1) {
                        OWrole = Player.roleHash(role.getName());
                    }
                }
                p = new Player(m, OWrole);
            }
            players.add(p);
            if (!inRoster(p)) {
                notResponded.add(p);

                System.out.println(String.format("[]: not in roster, adding", p.getMember().getUser().getName()));
            }
        }

        for (Player player : confimed) {
            if (!players.contains(player)) {
                confimed.remove(player);
                System.out.println(
                        String.format("[]: no longer in roster, removing", player.getMember().getUser().getName()));
            }
        }
        for (Player player : notResponded) {
            if (!players.contains(player)) {
                notResponded.remove(player);
                System.out.println(
                        String.format("[]: no longer in roster, removing", player.getMember().getUser().getName()));
            }
        }
        for (Player player : declined) {
            if (!players.contains(player)) {
                System.out.println(
                        String.format("[]: no longer in roster, removing", player.getMember().getUser().getName()));
                declined.remove(player);
                SubRequest req = getReqByPlayer(player);
                subRequests.remove(req);
                req.delete(req.getTrigger().getName() + " this player is no longer on this team. Sorry!");
            }
        }
    }

    public void updateEventMessage(DiscordBot bot, boolean addAsListener) {
        int c = 0;
        while (inqueue) {
            try {
                System.out.print("update event message queue: " + c++);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (DiscordBot.getQueue() == 0)
                inqueue = false;
        }
        inqueue = true;
        updateRoster();
        System.out.println(message == null ? "null msg" : "has msg");
        String[] types = { "Scrim", "AAOL", "Coaching", "Open Div" };
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        // if (sort) {
        // System.out.println("adding @s");
        // message.addContent(
        // event.getTeam().getRosterRole().getAsMention() +
        // event.getTeam().getTrialRole().getAsMention());
        // }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setAuthor(types[type]);
        embed.addField(
                new Field(String.format("<t:%s:F>", getUnix()), "(formatted to **your** timezone)", false));
        embed.addField(bot.getEmoji("1025285207273779270") + " Discord", disc, true);
        embed.addField(bot.getEmoji("1025284464097632307") + " Bnet", bnet, true);
        // System.out.println(event.getUnix());
        embed.addBlankField(false);
        embed.addField(bot.getEmoji("1042385181971058698") + " Confirmed", confirmed(), true);
        embed.addField(bot.getEmoji("1042385635610210334") + " Not Responded", notResponded(), true);
        embed.addField(bot.getEmoji("1042385172835860490") + " Declined", declined(), true);
        embed.addBlankField(false);
        embed.addField("subs", subs(), false);
        long eventKey = gethashCode();
        embed.setFooter(String.valueOf(eventKey));
        messageBuilder.addEmbeds(embed.build());
        // System.out.print("send: " + eventKey);
        messageBuilder.addActionRow(Button.primary(String.format("%s_%s", "ScrimButtYes", eventKey), "Yes"),
                Button.danger(String.format("%s_%s", "ScrimButtNo", eventKey),
                        "No"));
        messageData = messageBuilder.build();
        if (message == null) {
            // has not sent event
            DiscordBot.addQueue();
            team.getTimetable().sendMessage(messageData).queue((res) -> {
                message = res;
                System.out.println("sent scrim message");
                DiscordBot.subtractQueue();
                if (addAsListener) {
                    bot.addListener(this);
                    createJobs();
                }
                inqueue = false;
            });
        } else {
            DiscordBot.addQueue();
            message.editMessage(MessageEditData.fromCreateData(messageData)).queue((res) -> {
                DiscordBot.subtractQueue();
                this.message = res;
                System.out.println("updated scrim message");
                inqueue = false;
            });
        }
    }

    private SubRequest getReqByPlayer(Player p) {
        for (SubRequest req : subRequests) {
            if (req.getTrigger().getUserId().equalsIgnoreCase(p.getUserId())) {
                return req;
            }
        }
        return null;
    }

    public String subs() {
        System.out.println("called getSubString for event " + title);
        String res = " ";
        for (SubRequest req : subRequests) {
            res += req.toString();
        }
        System.out.println(res);
        return res;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent buttonEvent) {
        String[] data = buttonEvent.getButton().getId().split("_");
        System.out.println(data[1] + " == " + String.valueOf(gethashCode()));
        if (data[1].equalsIgnoreCase(String.valueOf(gethashCode()))) {
            if (!team.hasRosterOrTrialRole(buttonEvent.getMember())
                    && !isSub(Player.getPlayer(buttonEvent.getMember()))) {
                buttonEvent.reply("you dont have perms").setEphemeral(true).queue();
                return;
            }

            InteractionHook reply = buttonEvent.deferReply(true).complete();
            try {
                reply.editOriginal("processing").queue();
                Player p = Player.getPlayer(buttonEvent.getMember());

                if (data[0].equalsIgnoreCase("scrimbuttyes")) {
                    boolean wasSub = addConfirmed(p);
                    if (wasSub) {
                        SubRequest req = getReqByPlayer(p);
                        subRequests.remove(req);
                        req.delete(req.getTrigger().getName() + " can make the scrim. Thanks for subbing! -oli");
                    }
                    addConfirmed(p);
                    reply.editOriginal("Successfully accepted the scrim").queue();

                } else if (data[0].equalsIgnoreCase("scrimbuttno")) {
                    boolean alreadyDeclines = addDeclined(p);
                    if (alreadyDeclines)
                        throw new Exception("Already declined the scrim :>");
                    if (needsSub(p.getRole())) {
                        SubRequest req = new SubRequest(p, this);
                        DiscordBot.getInstance().addListener(req);
                        subRequests.add(req);
                        req.sendRequest();
                    }
                    reply.editOriginal("Successfully declined the scrim");
                }
                updateEventMessage(DiscordBot.getInstance(data), false);
                reply.deleteOriginal().queue();
            } catch (Exception e) {
                reply.editOriginal(e.getMessage()).queue();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent context) {
        System.out.println(toString());
        long key = 0;
        try {
            MessageEmbed embed = context.getTarget().getEmbeds().get(0);
            key = Long.parseLong(embed.getFooter().getText());
        } catch (Exception e) {
            e.printStackTrace();
            return;
            // TODO: handle exception
        }
        if (key == 0 || key != gethashCode())
            return;
        String command = context.getCommandString();
        if (command.equalsIgnoreCase("delete event")) {
            InteractionHook reply = context.deferReply(true).complete();
            reply.editOriginal("processing");
            try {
                deleteEvent("This scrim has been cancelled. Thanks for subbing!");
                reply.editOriginal("deleted").queue();
            } catch (Exception e) {
                context.reply(e.getMessage()).setEphemeral(true).queue();
                e.printStackTrace();
            }
        } else if (command.equalsIgnoreCase("edit details")) {
            SheetConfig config = team.getSheetConfig();
            if (config == null)
                config = team.getGuild().getSheetConfig();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(config.getDateFormatter(), Locale.US);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(config.getTimeFormatter(), Locale.US);
            TextInput title = TextInput.create("title", "team name", TextInputStyle.SHORT)
                    .setValue(this.title).build();
            TextInput date = TextInput.create("date", "date", TextInputStyle.SHORT)
                    .setValue(this.dateTime.format(dateFormatter)).build();
            TextInput time = TextInput.create("time", "time", TextInputStyle.SHORT)
                    .setValue(this.dateTime.format(timeFormatter)).build();
            TextInput disc = TextInput.create("disc", "discord poc", TextInputStyle.SHORT)
                    .setValue(this.disc).build();
            TextInput bnet = TextInput.create("bnet", "bnet poc", TextInputStyle.SHORT)
                    .setValue(this.bnet).build();
            context.replyModal(Modal.create(String.format("%s_editDetails", gethashCode()), "edit details")
                    .addActionRow(title)
                    .addActionRow(date)
                    .addActionRow(time)
                    .addActionRow(disc)
                    .addActionRow(bnet).build()).queue();
        } else if (command.equalsIgnoreCase("edit responses")) {
            TextInput confirmed = TextInput.create("confirmed", "confirmed", TextInputStyle.PARAGRAPH)
                    .setValue(confirmedString()).build();
            TextInput NR = TextInput.create("notresponded", "not responded", TextInputStyle.PARAGRAPH)
                    .setValue(notRespondedString()).build();
            TextInput declined = TextInput.create("declined", "declined", TextInputStyle.PARAGRAPH)
                    .setValue(declinedString()).build();
            context.replyModal(Modal.create(String.format("%s_editResponses", gethashCode()), "edit responses")
                    .addActionRow(confirmed)
                    .addActionRow(NR)
                    .addActionRow(declined).build()).queue();
        }

    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent modal) {
        if (!modal.getModalId().split("_")[0].equalsIgnoreCase(String.valueOf(gethashCode())))
            return;
        if (modal.getModalId().split("_")[1].equalsIgnoreCase("editDetails")) {
            String title = modal.getValue("title").getAsString();
            String date = modal.getValue("date").getAsString();
            String time = modal.getValue("time").getAsString();
            String disc = modal.getValue("disc").getAsString();
            String bnet = modal.getValue("bnet").getAsString();
            Event old = (Event) this.clone();
            this.title = title;
            this.disc = disc;
            this.bnet = bnet;

            SheetConfig config = team.getSheetConfig();
            if (config == null)
                config = team.getGuild().getSheetConfig();
            LocalTime lt = LocalTime.parse(time, DateTimeFormatter.ofPattern(config.getTimeFormatter(), Locale.US));
            LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern(config.getDateFormatter(), Locale.US));

            this.dateTime = ZonedDateTime.of(ld, lt, TimeZone.getTimeZone("Australia/Sydney").toZoneId());

            repository.remove(old.gethashCode());
            repository.put(gethashCode(), this);

            GoogleSheet2 sheet = new GoogleSheet2();
            try {
                sheet.updateEvent(this, old);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateEventMessage(DiscordBot.getInstance(), false);

        } else if (modal.getModalId().split("_")[1].equalsIgnoreCase("editResponses")) {
            String[] confirmed = modal.getValue("confirmed").getAsString().split(" ");
            String[] NR = modal.getValue("notresponded").getAsString().split(" ");
            String[] declined = modal.getValue("declined").getAsString().split(" ");

            for (String s : confirmed) {
                if (!s.equalsIgnoreCase("none")) {
                    Player p = Player.getPlayerByName(s);
                    if (p != null) {
                        boolean res = addConfirmed(p);
                        if (res) {
                            SubRequest req = getReqByPlayer(p);
                            subRequests.remove(req);
                            req.delete(req.getTrigger().getName() + " can make the scrim. Thanks for subbing! -oli");
                        }
                    }
                }
            }
            for (String s : NR) {
                if (!s.equalsIgnoreCase("none")) {
                    Player p = Player.getPlayerByName(s);
                    if (p != null) {
                        addNR(p);
                    }
                }
            }
            for (String s : declined) {
                if (!s.equalsIgnoreCase("none")) {
                    Player p = Player.getPlayerByName(s);
                    if (p != null) {
                        boolean res = addDeclined(p);
                        if (!res) {
                            if (needsSub(p.getRole())) {
                                SubRequest req = new SubRequest(p, this);
                                DiscordBot.getInstance().addListener(req);
                                subRequests.add(req);
                                req.sendRequest();
                            }
                        }
                    }
                }
            }
            updateEventMessage(DiscordBot.getInstance(), false);
        }
    }

    // public void updateScrim(DiscordBot bot) {
    // System.out.println(String.format("scrim id: ", message.getId()));
    // updateEventMessage(bot);
    // }

    @Override
    public int compareTo(Event o) {
        boolean res = dateTime.compareTo(o.getDateTime()) == 0;
        res = res && team.getName().equalsIgnoreCase(o.getTeam().getName());
        return res ? 1 : 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Player getDeclinedPlayerByRole(int role) {
        int numOfExisting = 0;
        for (SubRequest req : subRequests) {
            if (req.getSubRole() == role && req.getPlayer() == null && req.getMessage() != null)
                numOfExisting++;
        }
        int count = 0;
        for (Player p : declined) {
            if (p.getRole() == role) {
                if (count >= numOfExisting) {
                    return p;
                }
                count++;
            }
        }
        return null;
    }

    public void replaceSubRequest(SubRequest newReq) {
        int role = newReq.getSubRole();
        SubRequest oldReq = subRequests.getFirst();
        int index = 0;
        while (oldReq.getSubRole() != role && oldReq.getMessage() != null && oldReq.getPlayer() != null) {
            index++;
            oldReq = subRequests.get(index);
        }
        subRequests.remove(index);
        subRequests.add(newReq);
    }

    public void checkSubRequests() {
        System.out.println(String.format("[%s]: checking sub requests", title));
        for (SubRequest req : subRequests) {
            if (req.getMessage() == null && req.getPlayer() == null) {
                System.out.println(String.format("[%s]: sending req", title));
                req.sendRequest();
            }
        }
        System.out.println(String.format("[%s]: finished checking sub requests", title));
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String contact1) {
        this.disc = contact1;
    }

    public String getBnet() {
        return bnet;
    }

    public void setBnet(String contact2) {
        this.bnet = contact2;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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
            try {
                s += player.at + " - " + roles[player.getRole()] + "\n";
                
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
        }
        return s;
    }

    public String confirmedString() {
        String s = "";
        for (Player p : confimed) {
            s += p.getName() + " ";
        }
        if (s.equalsIgnoreCase(""))
            s = "none";
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
        if (s.equalsIgnoreCase(""))
            s = "none";
        return s;
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
        if (s.equalsIgnoreCase(""))
            s = "none";
        return s;
    }

    public void addSubRequest(SubRequest req) {
        subRequests.add(req);
    }

    public boolean addDeclined(Player player) {
        boolean res = removeFromLists(player);
        declined.add(player);
        return res;
    }

    public void addNR(Player player) {
        removeFromLists(player);
        notResponded.add(player);
    }

    public static Event getEvent(long key) {
        return repository.getOrDefault(key, null);
    }

    public static Event addEvent(long key, Event e) {
        return repository.put(key, e);
    }

    private boolean inRoster(Player p) {
        return confimed.contains(p) || notResponded.contains(p) || declined.contains(p);
    }

    // public void addSub(SubRequest sub) {
    // // subs.add(sub);
    // for (int i =0; i < 5; i++) {
    // if (subs[i] == null) {
    // subs[i] = sub;
    // }
    // }

    // }

    public boolean needsSub(int role) {
        int count = 0;

        for (Player player : confimed) {
            if (player.getRole() == role)
                count++;
        }
        // for (int i = 0; i < subs.length; i++) {
        // if (subs[i] != null && subs[i].getPlayer() != null) {
        // if (subs[i].getSubRole() == role)
        // count++;
        // }
        // }
        if (role == Player.TANK && count >= 1)
            return false;
        if (role == Player.DPS && count >= 2)
            return false;
        if (role == Player.SUPPORT && count >= 2)
            return false;
        return true;
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

    public static LinkedList<Event> getAllEvents() {
        return new LinkedList<>(repository.values());
    }

    public static Event getByTeamAndUnix(Team t, long unix) {
        LinkedList<Event> eventsList = new LinkedList<>(t.getEvents());
        for (Event e : eventsList) {
            if (e.getUnix() == unix)
                return e;
        }
        return null;

        // if (events.size() == 0) {
        // System.out.println("could not find event by team and unix");
        // return null;
        // } else if (events.size() == 1) {
        // return events.getFirst();
        // } else {
        // System.out.println("more than 1 event found when searching by team and
        // date");
        // for (Event e : events) System.out.println(e.getTitle());
        // return events.getFirst();
        // }
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

    // public static int virtualhashCode(String title, Team team, int type, long
    // unix) {
    // final int prime = 31;
    // int result = 1;
    // result = prime * result + ((title == null) ? 0 : title.hashCode());
    // // result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
    // // result = prime * result + ((contact1 == null) ? 0 : contact1.hashCode());
    // // result = prime * result + ((contact2 == null) ? 0 : contact2.hashCode());
    // result = prime * result + ((team == null) ? 0 : team.hashCode());
    // result = prime * result + type;

    // result += unix;
    // return result;
    // }

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
        if (disc == null) {
            if (other.disc != null)
                return false;
        } else if (!disc.equals(other.disc))
            return false;
        if (bnet == null) {
            if (other.bnet != null)
                return false;
        } else if (!bnet.equals(other.bnet))
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
                if (e != null)
                    res.add(e);
            } catch (Exception ex) {
                ex.printStackTrace();
                // TODO: handle exception
            }
        }

        return res;
    }

    // public boolean isSentReminders() {
    // return sentReminders;
    // }

    // public void setSentReminders(boolean sentReminders) {
    // this.sentReminders = sentReminders;
    // }

    public static int typeHash(String type) {
        switch (type.toLowerCase()) {
            case "scrim":
                return SCRIM;
            case "open div":
                return OPENDIV;
            case "coaching":
                return COACHING;
            case "aaol":
                return AAOL;
            default:
                return SCRIM;
        }
    }

    public List<List<Object>> toSheetValues() {
        SheetConfig config = team.getSheetConfig();
        if (config == null)
            config = team.getGuild().getSheetConfig();

        LinkedList<Object> res = new LinkedList<>();

        for (String orderElement : config.getOrder()) {
            if (orderElement.equalsIgnoreCase("title") && config.isCombinedNameandType()) {
                res.add(typeStrings[type] + config.getTitleDelimiter() + title);
            } else if (orderElement.equalsIgnoreCase("type") && !config.isCombinedNameandType()) {
                res.add(typeStrings[type]);
            } else if (orderElement.equalsIgnoreCase("team") && !config.isCombinedNameandType()) {
                res.add(title);
            } else if (orderElement.equalsIgnoreCase("time")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(config.getTimeFormatter(), Locale.US);
                res.add(dateTime.format(formatter));
            } else if (orderElement.equalsIgnoreCase("date")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/YYYY", Locale.US);
                res.add(dateTime.format(formatter));
            } else if (orderElement.equalsIgnoreCase("disc")) {
                res.add(disc);
            } else if (orderElement.equalsIgnoreCase("bent")) {
                res.add(bnet);
            }
        }
        List<List<Object>> out = new ListBuilder<>();
        out.add(res);
        return out;
    }

    public Event clone() {
        return new Event(title, dateTime, disc, bnet, team, type);
    }
}