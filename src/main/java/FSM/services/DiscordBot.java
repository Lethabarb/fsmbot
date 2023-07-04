package FSM.services;

import java.awt.Color;
import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import FSM.entities.Event;
import FSM.entities.Player;
import FSM.entities.Server;
import FSM.entities.SheetConfig;
import FSM.entities.SubRequest;
import FSM.entities.Team;
import FSM.entities.TeamDTO;
import FSM.entities.Comparators.EventComparator;
import FSM.entities.jobs.HeadPair;
import FSM.entities.jobs.NSW;
import FSM.entities.jobs.NT;
import FSM.entities.jobs.VIC;
import FSM.services.jobs.JobsService;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class DiscordBot extends ListenerAdapter {
    private static JDA bot;
    private static DiscordBot instance;
    private static int QUEUESIZE = 0;

    public synchronized static int getQueue() {
        return QUEUESIZE;
    }

    public synchronized static void addQueue() {
        QUEUESIZE++;
    }

    public synchronized static void subtractQueue() {
        QUEUESIZE--;
    }

    private DiscordBot(String... token) {
        if (bot == null) {
            JDABuilder jda = JDABuilder
                    .createDefault(token[0])
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT);
            jda.addEventListeners(this);

            // TODO: make event / sub request event listeners. This can make the code WAY
            // cleaner.

            // jda.lis/
            bot = jda.build();
            try {
                bot.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("%s is ready", bot.getSelfUser().getName()));
        }
        // logout();
    }

    public synchronized static DiscordBot getInstance(String... token) {
        if (instance == null) {
            instance = new DiscordBot(token);
        }
        return instance;
    }

    public synchronized Server makeGuild(String guildId, String subChannelId, String subRoleId, SheetConfig sheetConfig,
            TeamDTO... teams) {
        Guild guild = bot.getGuildById(guildId);
        MessageChannel subChannel = guild.getTextChannelById(subChannelId);
        Role subRole = guild.getRoleById(subRoleId);
        Server serv = new Server(guild, subChannel, subRole, sheetConfig);
        for (TeamDTO team : teams) {
            Team t = makeTeam(team.getName(), team.getNameAbbv(), team.getMinRank(), team.getTimetableId(),
                    team.getAnnounceId(),
                    team.getRosterRoleId(), team.getTrialRoleId(), team.getSubRoleId(), serv, team.getSubCalenderId(),
                    team.getSheetId(), team.getManagerId());
        }
        addListener(serv);
        return serv;
    }

    // public Team makeTeam(String name, String nameAbbv, String minRank, String
    // timetableId,
    // String rosterRoleId,
    // String trialRoleId, String subRoleId, Server s, int subCalenderId) {
    // MessageChannel timetable = bot.getTextChannelById(timetableId);
    // Role rosterRole = bot.getRoleById(rosterRoleId);
    // Role subRole = bot.getRoleById(subRoleId);
    // Role trialRole = bot.getRoleById(trialRoleId);
    // List<Member> mems = getMemberOfRole(s.getGuild(), trialRole, rosterRole);
    // Team t = new Team(name, nameAbbv, minRank, timetable, rosterRole, trialRole,
    // subRole, mems, subCalenderId);
    // t.setServer(s);
    // s.addTeam(t);
    // return t;
    // }

    public Team makeTeam(String name, String nameAbbv, String minRank, String timetableId, String announceId,
            String rosterRoleId,
            String trialRoleId, String subRoleId, Server s, int subCalenderId, String sheetId, String managerId) {
        MessageChannel timetable = bot.getTextChannelById(timetableId);
        MessageChannel announce = bot.getTextChannelById(announceId);
        Role rosterRole = bot.getRoleById(rosterRoleId);
        Role subRole = bot.getRoleById(subRoleId);
        Role trialRole = bot.getRoleById(trialRoleId);
        User manager = bot.getUserById(managerId);
        List<Member> mems = getMemberOfRole(s.getGuild(), trialRole, rosterRole);
        Team t = new Team(name, nameAbbv, minRank, timetable, announce, rosterRole, trialRole, subRole, mems,
                subCalenderId,
                sheetId, manager);
        t.setServer(s);
        s.addTeam(t);
        addListener(t);
        return t;
    }

    public synchronized void addListener(Object o) {
        bot.addEventListener(o);
    }

    public synchronized void updateScrims(Team t) {
        System.out.println("updating scrims for " + t.getName());
        try {
            // GoogleSheet sheet = new GoogleSheet();
            GoogleSheet2 sheet = new GoogleSheet2();
            LinkedList<Event> events = sheet.getEvents(t);
            for (int i = 0; i < events.size(); i++) {
                // sendEvent(events.get(i), true);
                System.out.println(events.get(i).getTitle());
                events.get(i).updateEventMessage(this, true);
            }
            System.out.println("done updating scrims");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean doesEventExist(Event event, MessageChannel c) {
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        for (Message message : messages) {
            try {
                String eventHash = message.getEmbeds().get(0).getFooter().getText();

                // MessageEmbed embed = message.getEmbeds().get(0);
                // String timeFormatted = embed.getFields().get(0).getName();
                // Long unix = Long.valueOf(timeFormatted.split(":")[1]);
                System.out.println(String.format("%s == %s", event.gethashCode(), Long.valueOf(eventHash)));
                System.out.println(Long.valueOf(eventHash).compareTo(event.gethashCode()));
                if (Long.valueOf(eventHash).compareTo(event.gethashCode()) == 0) {
                    System.out.println("exists");
                    return true;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return false;
    }

    public synchronized void sendLethabarbMessage(String s) {
        User u = bot.getUserById("251578157822509057");
        u.openPrivateChannel().queue((c) -> {
            c.sendMessage(s).queue();
        });
    }

    public synchronized void createEventsFromChanel(Team t) {
        MessageChannel c = t.getTimetable();
        // System.out.println("=========="+t.getName()+"==========");
        System.out.println("["+ t.getName() + "]: Finding existing scrims for ");
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        Event event = null;
        for (Message message : messages) {
            try {
                MessageEmbed embed = message.getEmbeds().get(0);
                String timeFormatted = embed.getFields().get(0).getName();
                // System.out.println(timeFormatted);
                Long unix = Long.valueOf(timeFormatted.split(":")[1]);
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix),
                        TimeZone.getTimeZone("Australia/Sydney").toZoneId());
                // ZonedDateTime dateTime =
                // LocalDateTime.ofInstant(Instant.ofEpochSecond(unix),);
                String title = embed.getTitle();
                String contact1 = embed.getFields().get(1).getValue();
                String contact2 = embed.getFields().get(2).getValue();
                int type = 0;
                switch (embed.getAuthor().getName()) {
                    case "Scrim":
                        type = Event.SCRIM;
                        break;
                    case "AAOL":
                        type = Event.AAOL;
                        break;
                    case "OD":
                        type = Event.OPENDIV;
                        break;
                    case "Coaching":
                        type = Event.COACHING;
                        break;
                    default:
                        break;
                }
                event = new Event(title, dateTime, message, contact1, contact2, t, type);
                t.addEvent(event);
                Event.addEvent(event.gethashCode(), event);
                bot.addEventListener(event);
                event.createJobs();
                LinkedList<Player> players = event.getNotResponded();
                String[] accepted = embed.getFields().get(4).getValue().split("\n");
                String[] declined = embed.getFields().get(6).getValue().split("\n");
                String[] subs = embed.getFields().get(8).getValue().split("\n");
                for (String userAt : accepted) {
                    if (userAt.length() > 1) {
                        String at = userAt.split(" - ")[0];
                        String id = at.substring(2, at.length() - 1);
                        Player p = Player.getPlayer(t.getServer().getGuild().getMemberById(id));
                        if (p != null) {
                            event.addConfirmed(p);
                        }
                    }
                }
                for (String userAt : declined) {
                    if (userAt.length() > 1) {
                        String id = userAt.substring(2, userAt.length() - 1);
                        Player p = Player.getPlayer(t.getServer().getGuild().getMemberById(id));
                        if (p != null) {
                            event.addDeclined(p);
                        }
                    }
                }
                for (String subString : subs) {
                    if (subString.length() > 1) {
                        String substituteAt = subString.split(" - ")[0];
                        String triggerAt = subString.split(" - ")[2];
                        if (substituteAt.equalsIgnoreCase("TBA")) {
                            String triggerId = triggerAt.substring(2, triggerAt.length() - 1);
                            Player trigger = Player.getPlayer(t.getServer().getGuild().getMemberById(triggerId));
                            SubRequest req = new SubRequest(trigger, event);
                            event.addSubRequest(req);
                        } else {
                            String substituteId = substituteAt.substring(2, substituteAt.length() - 1);
                            String triggerId = triggerAt.substring(2, triggerAt.length() - 1);

                            Player substitute = Player.getPlayer(t.getServer().getGuild().getMemberById(substituteId));
                            Player trigger = Player.getPlayer(t.getServer().getGuild().getMemberById(triggerId));
                            if (substitute == null) {
                                Member m = t.getServer().getGuild().getMemberById(substituteId);
                                substitute = new Player(m);
                            }
                            if (trigger == null) {
                                Member m = t.getServer().getGuild().getMemberById(triggerId);
                                substitute = new Player(m);
                            }
                            SubRequest sub = new SubRequest(trigger, substitute, event);
                            event.addSubRequest(sub);
                        }
                    }
                    event.setMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  finally {
            //     if (event != null)
            //         event.updateEventMessage(this, true);
            // }
        }
        System.out.println("["+ t.getName() + "]:finished finding scrims");
    }
    // public static void main(String[] args) {
    // DiscordBot bot = getInstance("");
    // String desName = "Ambition Desire";
    // String desNameAbbv = "Des";
    // String desMinRank = "Master5+";
    // String desTimetableId = "1099949798838243388";
    // String desAnnounceId = "1099949772397367387";
    // String desRosterRoleId = "1099948306760749147";
    // String desTrialRoleId = "1099948024844791918";
    // String desSubRoleId = "1099948114510618727";
    // int desSubCal = 11997719;

    // TeamDTO abitionDesire = new TeamDTO(desName, desNameAbbv, desMinRank,
    // desTimetableId, desAnnounceId, desRosterRoleId, desTrialRoleId, desSubRoleId,
    // desSubCal);

    // String fsmGuildId = "734267704516673536";
    // String fsmSubChannelId = "824447819690672132";
    // String fsmSubRoleId = "948413633182974032";

    // Server fsm = bot.makeGuild(fsmGuildId, fsmSubChannelId, fsmSubRoleId,
    // abitionDesire);

    // // MessageChannel c = bot.get
    // // bot.createSubReqestsFromChannel("824447819690672132");
    // }

    public void createSubReqestsFromChannel(MessageChannel c) {
        // MessageChannel c = bot.getTextChannelById(id);
        System.out.println("["+ c.getName() + "]: finding existing sub requests");
        System.out.println("Finding sub requests....");
        System.out.println("finding last message...");
        int size = 50;
        // boolean firstPass = true;
        String lastId = "";
        List<Message> messages = null;
        lastId = c.getLatestMessageId();
        if (!lastId.equalsIgnoreCase("")) {
            messages = new LinkedList<>(
                    MessageHistory.getHistoryAround(c, lastId).complete().getRetrievedHistory());
            Predicate<Message> pred = (Message m) -> (!m.getAuthor().isBot());
            System.out.println("Number of messages: " + messages.size());
            messages.removeIf(pred);
            System.out.println("Number of messages: " + messages.size());
            for (Message message : messages) {
                try {
                    String title = message.getEmbeds().get(0).getTitle();
                    System.out.println(title);
                    String unixString = title.split(":")[1];
                    System.out.println(title + " -> " + unixString);
                    long unix = Long.parseLong(unixString);
                    ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix),
                            TimeZone.getTimeZone("Australia/Sydney").toZoneId());
                    if (dt.compareTo(ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId())) > 0) {

                        // int width = 14;
                        String desc = message.getEmbeds().get(0).getDescription();
                        String[] descSplit = desc.split("\\*\\*");
                        String name = "";
                        for (String word : descSplit) {
                            if (!word.startsWith("<")) {
                                name = word;
                            }
                        }

                        Team team = Team.getTeamByName(name);
                        Event e = Event.getByTeamAndUnix(team, unix);
                        if (e != null) {
                            String UUID = message.getActionRows().get(0).getButtons().get(0).getId().split("_")[1];
                            String role = message.getEmbeds().get(0).getFields().get(0).getValue();
                            Player trigger = e.getDeclinedPlayerByRole(Player.roleHash(role));
                            SubRequest req = new SubRequest(UUID, e, Player.roleHash(role), trigger, message);
                            // e.addSubRequest(req);
                            e.replaceSubRequest(req);

                            //TODO: replace the existing req for same role in the event.
                            //      flag: null message
                            //      when checking, check to see if any reqs still have null messages
                            addListener(req);
                        } else {
                            message.delete().complete();
                        }
                    } else {
                        message.delete().complete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message.delete().complete();
                    // TODO: handle exception
                }
            }
        }
        System.out.println("["+ c.getName() + "]:finished");

    }

    public synchronized void sortChannel(MessageChannel c) {
        System.out.println("sorting...");
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        LinkedList<Message> messagesll = new LinkedList<>(messages);
        EventComparator comparator = new EventComparator();
        ArrayList<Event> sorted = Event.messagesToEvents(messages);
        sorted.sort(comparator);
        ArrayList<Event> events = Event.messagesToEvents(messages); // bottom up

        if (!sorted.isEmpty()) {
            int unsortedIndex = 0;
            for (int i = 0; i < sorted.size(); i++) {
                Event sortedEvent = sorted.get(i);
                Event notSortedEvent = events.get(events.size() - 1);

                System.out.println(sortedEvent.compareTo(notSortedEvent));
                if (sortedEvent.compareTo(notSortedEvent) == 1) {
                    messagesll.remove(messagesll.get(messagesll.size() - 1));
                    events.remove(notSortedEvent);
                    unsortedIndex++;
                } else {
                    i = sorted.size();
                }
            }
            for (Event e : events) {
                e.setMessage(null);
            }
            System.out.println("deleting messages...");
            System.out.println("getting GuildChannel");
            Guild guild = sorted.get(0).getTeam().getGuild().getGuild();
            GuildMessageChannel channel = guild.getTextChannelById(c.getId());

            if (messagesll.size() > 0) {
                for (Message message : messagesll) {
                    channel.deleteMessageById(message.getId()).queue();
                }
                for (int i = unsortedIndex; i < sorted.size(); i++) {
                    // sendEvent(sorted.get(i), false);
                    sorted.get(i).updateEventMessage(this, false);
                }
            }
        }
        System.out.println("finished.");

        // while (events.size() != 0) {
        // Event l = events.get(0);
        // ZonedDateTime lowest = events.get(0).getDateTime();
        // int lowIndex = 0;
        // for (int i = 1; i < events.size(); i++) {
        // Event e = events.get(i);
        // if (e.getDateTime().isBefore(lowest)) {
        // lowest = e.getDateTime();
        // l = e;
        // lowIndex = i;
        // }
        // }
        // if (lowIndex != events.size() - 1) {
        // // delete + add to sort if its not already sorted
        // c.deleteMessageById(l.getMessage().getId()).queue();

        // sorted.add(l);
        // }
        // events.remove(l);

        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // for (Event event : sorted) {
        // sendEvent(event, false);
        // }
        System.out.println("done sorting");
    }

    public String getEmoji(String id) {
        return bot.getEmojiById(id).getAsMention();
    }

    public synchronized List<Member> getMemberOfRole(Guild g, Role... r) {
        List<Member> members = new LinkedList<>();
        for (Role role : r) {
            members.addAll(g.getMembersWithRoles(role));
        }

        return new LinkedList<>(new LinkedHashSet<>(members));
    }

    public synchronized void giveMemberRole(Guild g, Member m, Role r) {
        g.addRoleToMember(m, r).queue();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    public synchronized void removeMemberRole(Guild g, Member m, Role r) {
        g.removeRoleFromMember(m, r).queue();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        ;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
        Guild guild = buttonEvent.getGuild();
        Button b = buttonEvent.getButton();

        String[] buttonData = b.getId().split("_");
        String buttonUse = buttonData[0];
        String Data = buttonData[1];
        // System.out.println("key: " + eventKey);
        Player trigger = Player.getPlayer(buttonEvent.getMember());

        if (buttonUse.equals("ScrimButtYes")) {
            // Event event = Event.getEvent(Long.parseLong(Data));
            // if (hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
            // buttonEvent.reply("You have accepted the scrim on <t:" + event.getUnix() +
            // ":F>").setEphemeral(true)
            // .queue();
            // System.out.println(event.getTeam().getName() + ": " +
            // buttonEvent.getMember().getUser().getName()
            // + "accepted scrim on " + event.getDateTime().toString());
            // boolean wasSub = event.addConfirmed(trigger);
            // if (wasSub) {
            // System.out.println("found sub for this role");
            // try {
            // if (trigger.getRole() == -1) {
            // buttonEvent.reply(
            // "you dont have a valid overwatch role for this event, please contact your
            // manager");
            // return;
            // }
            // // int sub = event.getExistingSub(trigger.getRole(), false);
            // try {
            // deleteSubRequest(event, trigger.getRole(), false);
            // // event.removeSub(sub);
            // } catch (Exception e) {
            // System.out.println("no sub message found");
            // }
            // } catch (Exception e) {

            // }
            // }

            // event.updateEventMessage(this);
            // } else {
            // buttonEvent.reply("you are not on the roster!").setEphemeral(true).queue();
            // try {
            // Thread.sleep(2000);
            // } catch (Exception e) {
            // // TODO: handle exception
            // }
            // }
        } else if (buttonUse.equals("ScrimButtNo")) {
            // Event event = Event.getEvent(Long.parseLong(Data));
            // System.out.println(event.getTeam().getName() + ": " +
            // buttonEvent.getMember().getUser().getName()
            // + "declined scrim on " + event.getDateTime().toString());

            // if (hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
            // event.addDeclined(trigger);
            // event.updateEventMessage(this);
            // if (event.needsSub(trigger.getRole())) {
            // System.out.println("sending sub req");
            // sendSubRequest(event, trigger.getRole());
            // } else {
            // System.out.println("role filled, no sub needed");
            // }
            // buttonEvent.reply("You have declined the scrim on <t:" + event.getUnix() +
            // ":F>").setEphemeral(true)
            // .queue();
            // } else if (event.isSub(trigger)) {
            // SubRequest req = SubRequest.getRequestByPlayer(event, trigger);
            // Member m = req.getPlayer().getMember();
            // if (req.deleteRequest()) {
            // buttonEvent.reply("you are no longer subbing");
            // removeMemberRole(guild, m, event.getTeam().getSubRole());
            // }
            // } else {
            // buttonEvent.reply("you are not on the roster!").setEphemeral(true).queue();
            // // try {
            // // Thread.sleep(2000);
            // // } catch (Exception e) {
            // // // TODO: handle exception
            // // }
            // }
        } else if (buttonUse.equals("Sub")) {
            // System.out.println(String.format("sub for subId: %s", Data));
            // SubRequest req = SubRequest.getRequest(Data);
            // Event event = req.getEvent();
            // // Event event = Event.getEvent(Long.parseLong(Data));
            // // int subIndex = Integer.parseInt(buttonData[2]);
            // // int role = Integer.parseInt(buttonData[3]);
            // if (trigger == null) {
            // trigger = new Player(buttonEvent.getMember(), req.getSubRole());
            // }
            // if (!hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
            // // event.setSubPlayer(subIndex, trigger);
            // // SubRequest req = SubRequest.getRequest(Data);
            // req.setPlayer(trigger);
            // deleteSubRequest(event, req.getSubRole(), true);
            // giveMemberRole(event.getTeam().getServer().getGuild(),
            // buttonEvent.getMember(),
            // event.getTeam().getSubRole());
            // // event.updateScrim();
            // updateAllEvents(event.getTeam());
            // User u = trigger.getMember().getUser();

            // MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            // EmbedBuilder embed = new EmbedBuilder();
            // Field team = new Field("Team", event.getTeam().getName(), true);
            // Field time = new Field("Time", "<t:" + event.getUnix() + ":F>", true);
            // Field channel = new Field("Where",
            // event.getTeam().getTimetable().getAsMention(), true);
            // embed.addField(team);
            // embed.addField(time);
            // embed.addField(channel);
            // messageBuilder.addEmbeds(embed.build());
            // messageBuilder.addActionRow(Button.primary(
            // String.format("%s_%s", "Sub", req.getUuid()),
            // "cancel"));

            // u.openPrivateChannel().complete().sendMessage(messageBuilder.build()).queue();
            // buttonEvent.reply("you are now subbing").setEphemeral(true).queue();
            // try {
            // Thread.sleep(2000);
            // } catch (Exception e) {
            // // TODO: handle exception
            // }
            // event.updateEventMessage(this);
            // } else {
            // buttonEvent.reply("you are on the roster! how can u be a
            // sub??").setEphemeral(true).queue();
            // try {
            // Thread.sleep(2000);
            // } catch (Exception e) {
            // // TODO: handle exception
            // }
            // }
        } else if (buttonUse.equalsIgnoreCase("editserverconfig")) {
            buttonEvent.getChannel().sendMessage("edit Server Config").queue();

        } else if (buttonUse.equalsIgnoreCase("serverduelsheets")) {
            buttonEvent.getChannel().sendMessage("etoggle duel sheets").queue();

        } else if (buttonUse.equalsIgnoreCase("uniqueteamsheets")) {
            buttonEvent.getChannel().sendMessage("toggle unique sheets").queue();

        } else if (buttonUse.equalsIgnoreCase("editTeamConfig")) {
            String teamRosterRoleId = Data;
            Role r = guild.getRoleById(teamRosterRoleId);
            Team t = Team.getTeamByRosterRole(r);
            buttonEvent.getChannel().sendMessage("editing " + t.getName()).queue();

        }
    }

    public synchronized void updateAllEvents(Team t) {
        System.out.println(String.format("[%s]: updating all events", t.getName()));
        LinkedList<Event> events = Event.getAllEvents();
        for (Event event : events) {
            if (event.getTeam().getName().equalsIgnoreCase(t.getName())) {
                event.updateEventMessage(this, false);
            }
        }
        System.out.println(String.format("[%s]: finished", t.getName()));
    }

    // @Override
    // public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent
    // commandEvent) {
    // String command = commandEvent.getName();
    // System.out.println(commandEvent.getMember().getUser().getName());
    // if (command.equals("update")) {
    // String subCommand = commandEvent.getSubcommandName();
    // Role role = commandEvent.getOption("teamrole").getAsRole();
    // if (subCommand.equals("events")) {
    // System.out.println(commandEvent.getMember().getUser().getName() + " called
    // /update events");
    // Team t = Team.getTeamByRosterRole(role);
    // updateAllEvents(t);
    // commandEvent.reply("events have been updated").setEphemeral(true).queue();
    // try {
    // Thread.sleep(2000);
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    // }
    // } else if (command.equals("role")) {
    // Server s = Server.getGuild(commandEvent.getGuild().getIdLong());
    // String roleName =
    // commandEvent.getOption("newplayerrole").getAsRole().getName();
    // Member member = commandEvent.getOption("playerdiscord").getAsMember();
    // Role role = commandEvent.getOption("newplayerrole").getAsRole();
    // changeRoles(member, s.getGuild());
    // s.getGuild().addRoleToMember(member, role);
    // Player p = Player.getPlayer(member);
    // p.setRole(Player.roleHash(roleName));
    // // updateAllEvents();
    // } else if (command.equals("sort")) {
    // MessageChannel c = commandEvent.getMessageChannel();
    // sortChannel(c);
    // } else if (command.equals("makeconfigchannel")) {
    // InteractionHook reply = commandEvent.deferReply(true).complete();
    // Server s = Server.getGuild(commandEvent.getGuild().getIdLong());
    // List<GuildChannel> channels = s.getGuild().getChannels(false);
    // LinkedList<GuildChannel> channelsll = new LinkedList<>();
    // for (GuildChannel guildChannel : channels) {
    // channelsll.add(guildChannel);
    // }
    // // GuildChannel chan = channels.get(0).getName()
    // Predicate<GuildChannel> pred = (GuildChannel gc) ->
    // (gc.getType().compareTo(ChannelType.TEXT) != 0
    // || !gc.getName().equalsIgnoreCase("fsm-config"));
    // Boolean found = channelsll.removeIf(pred);
    // if (channelsll.size() > 0) {

    // reply.editOriginal(channelsll.get(0).getAsMention() + "already exists.
    // editing channel").queue();
    // if (s.getBotConfigChannel() == null) {
    // MessageChannel c =
    // s.getGuild().getTextChannelById(channelsll.get(0).getIdLong());
    // s.setBotConfigChannel(c);
    // // commandEvent.reply("found existing channel").setEphemeral(true).queue();
    // List<Message> messages =
    // MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
    // for (Message message : messages) {
    // c.deleteMessageById(message.getId()).complete();
    // }
    // }
    // } else {
    // MessageChannel c = s.getGuild().createTextChannel("fsm-config").complete();
    // s.setBotConfigChannel(c);
    // }
    // // String sheetId = commandEvent.getOption("sheetid").getAsString();
    // MessageChannel c = s.getBotConfigChannel();
    // MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
    // String grayDivider = "1043359291542872104";
    // String[] colouredDivieders = { "1043359280587350076", "1043359279014482011",
    // "1043359277441618030", "1043359275403194450", "1043359283863113768",
    // "1043359282227331193",
    // "1043359287541510234", "1043359289173102632", "1043359285469528135" };
    // int colourCount = 0;
    // messageBuilder.addContent(
    // getEmoji(grayDivider).repeat(7) + "Server Configuration" +
    // getEmoji(grayDivider).repeat(7));
    // EmbedBuilder embed = new EmbedBuilder();
    // embed.setAuthor("FSM BOT");
    // embed.setTitle(s.getGuild().getName());
    // Field subRole = new Field("Sub Role", s.getSubRole().getAsMention(), true);
    // Field subRoleId = new Field("Sub Role Id", s.getSubRole().getId(), true);
    // Field subChannel = new Field("Sub Channel", s.getSubChannel().getAsMention(),
    // true);
    // Field subChannelId = new Field("Sub Channel Id", s.getSubChannel().getId(),
    // true);
    // // Field duelScheduleSheet = new Field("Duel Schedule Sheet", "false", true);
    // Field DifferentTeamSheetSetups = new Field("different team sheet setups",
    // "false", true);
    // Field sheetID = new Field("", "", false);
    // Field sheetPage = new Field("", "", false);
    // Field startCell = new Field("", "", false);
    // Field direction = new Field("", "", false);
    // Field step = new Field("", "", false);
    // Field combinedNameAndType = new Field("", "", false);
    // Field order = new Field("", "", false);
    // Field eventSize = new Field("", "", false);
    // String sheetJSON =
    // "{\"SheetID\":\"1HXcsb3Yt2tad_38UqIiAhFePZQ4-g-mMqIGYfLxnYcM\",\"SheetPage\":\"Event
    // Input\",\"Start\":\"2B\",\"Direction\":\"right\",\"Step\":-1,\"CombinedNameAndType\":true,\"Order\":[\"Title\",\"Time\",\"Date\",\"Disc\",\"bnet\"],\"EventSize\":3}";
    // // Field sheetConfig = new Field("google sheet ID", sheetJSON, false);
    // embed.addField(subRole);
    // embed.addField(subRoleId);
    // embed.addBlankField(false);
    // embed.addField(subChannel);
    // embed.addField(subChannelId);
    // embed.addBlankField(false);
    // // embed.addField(duelScheduleSheet);
    // embed.addField(DifferentTeamSheetSetups);
    // embed.addField(sheetID);
    // embed.addField(sheetPage);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // embed.addField(sheetID);
    // messageBuilder.addEmbeds(embed.build());
    // messageBuilder.addActionRow(Button.primary("editServerConfig_.", "edit"));
    // messageBuilder.addActionRow(Button.danger("serverDuelSheets_.", "toggle duel
    // sheet setup"),
    // Button.success("uniqueTeamSheets_.", "toggle unique team sheets"));

    // c.sendMessage(messageBuilder.build()).queue();

    // List<Team> teams = s.getTeamsAsList();
    // for (Team t : teams) {
    // messageBuilder = new MessageCreateBuilder();
    // messageBuilder.addContent(getEmoji(colouredDivieders[colourCount]).repeat(7)
    // + t.getName()
    // + getEmoji(colouredDivieders[colourCount]).repeat(7));
    // embed = new EmbedBuilder();
    // embed.setAuthor("FSM BOT");
    // embed.setTitle("Team Info");
    // // t.get
    // // roster role, trial role, min rank, name abbv, sub role, timetable channel,
    // // teamup sub calendar
    // Field rosterRole = new Field("Roster Role", t.getRosterRole().getAsMention(),
    // true);
    // Field rosterRoleId = new Field("Roster Role Id", t.getRosterRole().getId(),
    // true);
    // Field TrialRole = new Field("Trial Role", t.getTrialRole().getAsMention(),
    // true);
    // Field TrialRoleId = new Field("Trial Role Id", t.getTrialRole().getId(),
    // true);
    // Field minRank = new Field("Min Rank", t.getMinRank(), true);
    // Field nameAbbv = new Field("Short Name", t.getNameAbbv(), true);
    // Field teamSubRole = new Field("Roster Role", t.getSubRole().getAsMention(),
    // true);
    // Field teamSubRoleId = new Field("Roster Role", t.getSubRole().getId(), true);
    // Field timetableChannel = new Field("Timetable Channel",
    // t.getTimetable().getAsMention(), true);
    // Field timetableChannelId = new Field("Timetable Channel Id",
    // t.getTimetable().getId(), true);
    // Field teamUp = new Field("teamup subcal ID",
    // String.valueOf(t.getTeamupSubCalendar()), true);

    // embed.addField(nameAbbv);
    // embed.addField(minRank);
    // embed.addField(teamUp);
    // embed.addBlankField(false);
    // embed.addField(rosterRole);
    // embed.addField(rosterRoleId);
    // embed.addBlankField(false);
    // embed.addField(TrialRole);
    // embed.addField(TrialRoleId);
    // embed.addBlankField(false);
    // embed.addField(teamSubRole);
    // embed.addField(teamSubRoleId);
    // embed.addBlankField(false);
    // embed.addField(timetableChannel);
    // embed.addField(timetableChannelId);

    // messageBuilder.addEmbeds(embed.build());

    // messageBuilder.addActionRow(Button.primary("editTeamConfig_" +
    // t.getRosterRole().getId(), "edit"));

    // c.sendMessage(messageBuilder.build()).queue();

    // messageBuilder = new MessageCreateBuilder();
    // messageBuilder.addActionRow(Button.primary("newTeam", "add team"));
    // reply.editOriginal("Created Channel").queue();

    // }
    // } else if (command.equalsIgnoreCase("removesubs")) {
    // try {
    // Team t =
    // Team.getTeamByRosterRole(commandEvent.getOption("teamrole").getAsRole());
    // Guild g = commandEvent.getGuild();
    // removeSubs(g, t);
    // commandEvent.reply("removed subs for team " +
    // t.getRosterRole().getAsMention()).setEphemeral(true)
    // .queue();
    // } catch (Exception e) {
    // commandEvent.reply("team does not exist within the fsm bot");
    // // TODO: handle exception
    // }
    // }
    // }

    // @Override
    // public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent context) {
    //     Message message = context.getTarget();
    //     String command = context.getName();
    //     System.out.println("Context: " + command);
    //     System.out.println(context.getMember().getUser().getName());
    //     if (message.getAuthor().getName().equals(bot.getSelfUser().getName())) {
    //         MessageEmbed embed = message.getEmbeds().get(0);
    //         Event event = Event.getEvent(Long.parseLong(embed.getFooter().getText()));
    //         TextInput dateTime = TextInput.create("datetime", "event date + time", TextInputStyle.SHORT)
    //                 .setValue(event.getDateTime().format(DateTimeFormatter.ofPattern("E dd/MM/yyyy h:mm a", Locale.US)))
    //                 .build();
    //         TextInput eventhash = TextInput.create("hash", "hash (DONT EDIT)", TextInputStyle.SHORT)
    //                 .setValue(String.valueOf(event.gethashCode()))
    //                 .build();
    //         TextInput bnetPoc = TextInput.create("bnet", "bnet", TextInputStyle.SHORT)
    //                 .setValue(event.getBnet()).build();
    //         TextInput discPoc = TextInput.create("disc", "disc", TextInputStyle.SHORT)
    //                 .setValue(event.getDisc()).build();
    //         TextInput teamName = TextInput.create("name", "team against", TextInputStyle.SHORT)
    //                 .setValue(event.getTitle()).build();

    //         TextInput confirmed;
    //         try {
    //             confirmed = TextInput.create("confirmed", "confirmed", TextInputStyle.PARAGRAPH)
    //                     .setValue(event.confirmedString())
    //                     .build();
    //         } catch (Exception e) {
    //             confirmed = TextInput.create("confirmed", "confirmed", TextInputStyle.PARAGRAPH)
    //                     .setPlaceholder("confirmed players")
    //                     .build();
    //         }
    //         TextInput NR;
    //         try {
    //             NR = TextInput.create("notresponded", "not responded", TextInputStyle.PARAGRAPH)
    //                     .setValue(event.notRespondedString())
    //                     .build();
    //         } catch (Exception e) {
    //             NR = TextInput.create("notresponded", "not responded", TextInputStyle.PARAGRAPH)
    //                     .setPlaceholder("not responded players")
    //                     .build();
    //         }
    //         TextInput declined;
    //         try {
    //             declined = TextInput.create("declined", "declined", TextInputStyle.PARAGRAPH)
    //                     .setValue(event.declinedString())
    //                     .build();
    //         } catch (Exception e) {
    //             declined = TextInput.create("declined", "declined", TextInputStyle.PARAGRAPH)
    //                     .setPlaceholder("declined players")
    //                     .build();
    //         }

    //         Modal modal = null;
    //         if (command.equalsIgnoreCase("edit responses")) {
    //             modal = Modal.create("eventeditresponses", "Event Edit: " + event.gethashCode())
    //                     .addActionRows(ActionRow.of(eventhash),
    //                             ActionRow.of(confirmed),
    //                             ActionRow.of(NR),
    //                             ActionRow.of(declined))
    //                     .build();

    //         } else if (command.equalsIgnoreCase("edit details")) {
    //             modal = Modal.create("eventeditdetails", "Event Edit: " + event.gethashCode())
    //                     .addActionRows(ActionRow.of(eventhash),
    //                             ActionRow.of(bnetPoc),
    //                             ActionRow.of(discPoc),
    //                             ActionRow.of(teamName),
    //                             ActionRow.of(dateTime))
    //                     .build();
    //         }
    //         if (modal != null) {
    //             context.replyModal(modal).queue();
    //         }

    //     } else {
    //         context.reply("this isnt an event message").setEphemeral(true).queue();

    //     }
    // }

    // @Override
    // public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
    // System.out.println(event.getMember().getUser().getName());

    // if (event.getModalId().equals("eventeditresponses")) {
    // String confirmedString = event.getValue("confirmed").getAsString();
    // String notRespondedString = event.getValue("notresponded").getAsString();
    // String declinedString = event.getValue("declined").getAsString();
    // String eventHash = event.getValue("hash").getAsString();

    // Event scrim = Event.getEvent(Long.valueOf(eventHash));
    // // Event.removeFromRepository(scrim);
    // // ZonedDateTime datetime = LocalDateTime.parse(dateTimeString,
    // // DateTimeFormatter.ofPattern("E dd/MM/yyyy h:mm a", Locale.US))
    // // .atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
    // // scrim.setDateTime(datetime);

    // // confirmedString.replaceAll(" ", "");
    // // notRespondedString.replaceAll(" ", "");
    // // declinedString.replaceAll(" ", "");
    // for (String s : notRespondedString.split(" ")) {
    // Member member = null;
    // for (Member m : scrim.getTeam().getMembers()) {
    // System.out.println(String.format("'%s' == '%s'", s, m.getUser().getName()));
    // if (m.getUser().getName().equals(s.trim())) {
    // System.out.print("set member");
    // member = m;
    // }
    // }
    // if (member != null) {
    // Player p = Player.getPlayer(member);
    // if (p != null) {
    // scrim.addNR(p);
    // } else {
    // System.out.println("p is null 614");
    // }
    // } else {
    // System.out.println("m is null 617");
    // }
    // }

    // for (String s : confirmedString.split(" ")) {
    // Member member = null;
    // for (Member m : scrim.getTeam().getMembers()) {
    // if (m.getUser().getName().equals(s.trim())) {
    // member = m;
    // }
    // }
    // if (member != null) {
    // Player p = Player.getPlayer(member);
    // if (p != null) {
    // scrim.addConfirmed(p);
    // } else {
    // System.out.println("p is null 569");
    // }
    // } else {
    // System.out.println("m is null 599");
    // }
    // }
    // for (String s : declinedString.split(" ")) {
    // Member member = null;
    // for (Member m : scrim.getTeam().getMembers()) {
    // if (m.getUser().getName().equals(s.trim())) {
    // member = m;
    // }
    // }
    // if (member != null) {
    // Player p = Player.getPlayer(member);
    // if (p != null) {
    // scrim.addDeclined(p);
    // } else {
    // System.out.println("p is null 632");
    // }
    // } else {
    // System.out.println("m is null 635");
    // }
    // }
    // System.out.print(scrim.notRespondedString());

    // Event.addEvent(scrim.gethashCode(), scrim);
    // scrim.updateEventMessage(this);
    // GoogleSheet2 sheet = scrim.getTeam().getSheet();
    // sheet.updateEvent(scrim.getTeam().getNameAbbv(), scrim);
    // event.reply("Thanks for your request!").setEphemeral(true).queue();

    // } else if (event.getModalId().equals("eventeditdetails")) {
    // String eventHash = event.getValue("hash").getAsString();
    // String dateTimeString = event.getValue("datetime").getAsString();
    // String bnet = event.getValue("bnet").getAsString();
    // String disc = event.getValue("disc").getAsString();
    // String teamName = event.getValue("name").getAsString();
    // Event scrim = Event.getEvent(Long.valueOf(eventHash));
    // scrim.setDisc(disc);
    // scrim.setBnet(bnet);
    // scrim.setTitle(teamName);

    // Event.removeFromRepository(scrim);
    // ZonedDateTime datetime = LocalDateTime.parse(dateTimeString,
    // DateTimeFormatter.ofPattern("E dd/MM/yyyy h:mm a", Locale.US))
    // .atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
    // scrim.setDateTime(datetime);
    // Event.addEvent(scrim.gethashCode(), scrim);
    // scrim.updateEventMessage(this);
    // GoogleSheet sheet = scrim.getTeam().getSheet();
    // sheet.updateEvent(scrim.getTeam().getNameAbbv(), scrim);
    // event.reply("Thanks for your request!").setEphemeral(true).queue();
    // }
    // }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // String respo": [],nse = "";
        MessageChannel c = event.getChannel();
        if (event.getChannel().getType().compareTo(ChannelType.PRIVATE) == 0) {
            if (event.getAuthor().getName().equalsIgnoreCase("charweyyy")
                    || event.getAuthor().getName().equalsIgnoreCase("lethabarb")) {
                if (event.getMessage().getContentStripped().equalsIgnoreCase("jobs")) {

                    JobsService<NSW> NSW = new JobsService<NSW>("https://iworkfor.nsw.gov.au/Ajax/SearchJob",
                            "SearchKey", "ListItem", "Data", "Result");
                    HeadPair pageSize = new HeadPair("PageSize", "100");
                    HeadPair pageNum = new HeadPair("Page", "1");
                    NSW.getJobs("NSW", NSW[].class, pageSize, pageNum);
                    pageNum = new HeadPair("Page", "2");
                    NSW.getJobs("NSW", NSW[].class, pageSize, pageNum);

                    JobsService<VIC> VIC = new JobsService<>("https://careers.vic.gov.au/Ajax/SearchJob", "SearchKey",
                            "ListItem", "Data", "Result");
                    pageNum = new HeadPair("Page", "1");
                    VIC.getJobs("VIC", VIC[].class, pageSize, pageNum);
                    // pageNum = new HeadPair("Page", "2");
                    // VIC.getJobs("VIC", VIC[].class, pageSize, pageNum);

                    String NTJson = "{\"AgencyList\":null,\"CategoryList\":null,\"LocationsList\":null,\"VacanycyTypeList\":[{\"Disabled\":false,\"Group\":null,\"Selected\":false,\"Text\":\"Ongoing (Permanent) - Full Time\",\"Value\":\"991\"},{\"Disabled\":false,\"Group\":null,\"Selected\":false,\"Text\":\"Ongoing (Permanent) - Part Time\",\"Value\":\"992\"},{\"Disabled\":false,\"Group\":null,\"Selected\":false,\"Text\":\"Fixed (Temporary) - Full Time\",\"Value\":\"993\"},{\"Disabled\":false,\"Group\":null,\"Selected\":false,\"Text\":\"Fixed (Temporary) - Part Time\",\"Value\":\"994\"},{\"Disabled\":false,\"Group\":null,\"Selected\":false,\"Text\":\"Casual\",\"Value\":\"995\"}],\"VacancyNumber\":null,\"Keyword\":\"<>\",\"SelectedAgencyList\":[],\"SelectedCategoryList\":[],\"SelectedLocationsList\":[],\"RemunerationRangeFrom\":null,\"RemunerationRangeTo\":null,\"SelectedVacanycyType\":null,\"DateAdvertisedAfter\":null,\"SalaryRangeFrom\":null,\"SalaryRangeTo\":null,\"JobAlertID\":null,\"JobAlertName\":null,\"EmailAlert\":null,\"results\":null,\"jobAlerts\":[]}";
                    JobsService<NT> NT = new JobsService<>("https://jobs.nt.gov.au/Home/Search", "", "data");
                    NT.getJobsWithCustomSearch("NT", NTJson, NT[].class);

                    // String searchJSON =
                    // "{\"actions\":[{\"id\":\"89;a\",\"descriptor\":\"aura://ApexActionController/ACTION$execute\",\"callingDescriptor\":\"UNKNOWN\",\"params\":{\"namespace\":\"\",\"classname\":\"aps_jobSearchController\",\"method\":\"retrieveJobListings\",\"params\":{\\\"filter\\\":\\\"{\\\"searchString\\\":\\\"<>\\\",\\\"salaryFrom\\\":null,\\\"salaryTo\\\":null,\\\"closingDate\\\":null,\\\"positionInitiative\\\":null,\\\"classification\\\":null,\\\"securityClearance\\\":null,\\\"officeArrangement\\\":null,\\\"duration\\\":null,\\\"department\\\":null,\\\"category\\\":null,\\\"opportunityType\\\":null,\\\"employmentStatus\\\":null,\\\"state\\\":null,\\\"sortBy\\\":null,\\\"offset\\\":><,\\\"offsetIsLimit\\\":false,\\\"lastVisitedId\\\":null,\\\"daysInPast\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"notificationsEnabled\\\":null,\\\"savedSearchId\\\":null}&requested=Thu
                    // Feb 23 2023 12:27:32 GMT+1100 (Australian Eastern Daylight
                    // Time)\"},\"cacheable\":false,\"isContinuation\":false}}]}";

                    // JobsService<APS> APS = new
                    // JobsService<>("https://www.apsjobs.gov.au/s/sfsites/aura?r=1&aura.ApexAction.execute=1",
                    // "message", "jobListings", "actions", "returnValue", "returnValue");
                    // HeadPair auraContext = new HeadPair("aura.context",
                    // "{\"mode\":\"PROD\",\"fwuid\":\"GVQSDds1N8x8l9AfZLjrQg\",\"app\":\"siteforce:communityApp\",\"loaded\":{\"APPLICATION@markup://siteforce:communityApp\":\"Q-CTn3sb841JAb-fQMyOLA\",\"COMPONENT@markup://instrumentation:o11ySecondaryLoader\":\"NAR59T88qTprOlgZG3yLoQ\"},\"dn\":[],\"globals\":{},\"uad\":false}");
                    // HeadPair auraToken = new HeadPair("aura.token", "null");
                    // APS.getJobsWithCustomSearch("APS", searchJSON, APS[].class, 0, auraContext,
                    // auraToken);
                    // APS.getJobsWithCustomSearch("APS", searchJSON, APS[].class, 15, auraContext,
                    // auraToken);
                    // APS.getJobsWithCustomSearch("APS", searchJSON, APS[].class, 30, auraContext,
                    // auraToken);
                    // APS.getJobsWithCustomSearch("APS", searchJSON, APS[].class, 45, auraContext,
                    // auraToken);
                    // APS.getJobsWithCustomSearch("APS", searchJSON, APS[].class, 60, auraContext,
                    // auraToken);

                    JobsService.sendSheet();

                    c.sendFiles(FileUpload.fromData(new File("jobs.xlsx"))).queue();
                }
            }
        }
    }

    public void changeRoles(Member m, Guild g) {
        for (Role r : m.getRoles()) {
            if (Player.roleHash(r.getName()) != -1) {
                g.removeRoleFromMember(m, r);
            }
        }
    }

    public void removeSubs(Guild g, Team t) {
        Role subRole = t.getSubRole();
        List<Member> members = g.getMembersWithRoles(subRole);
        for (Member member : members) {
            g.removeRoleFromMember(member, subRole).queue();
        }
    }
}
