package FSM.services;

import java.awt.Color;
import java.io.File;
import java.lang.StackWalker.Option;
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

import com.google.gson.Gson;

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
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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

            bot = jda.build();
            try {
                bot.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("%s is ready", bot.getSelfUser().getName()));
            System.out.println("finding servers...");
            for (Guild guild : bot.getGuilds()) {
                System.out.print(guild.getName());
                // find fsm-config
                MessageChannel fsmConfig = null;
                List<TextChannel> channels = guild.getTextChannels();
                for (int i = 0; i < channels.size(); i++) {
                    TextChannel c = channels.get(i);
                    if (c.getName().equalsIgnoreCase("fsm-config")) {
                        fsmConfig = c;
                        i = channels.size();
                    }
                }
                // bot is active in server
                if (fsmConfig != null) {
                    try {
                        List<Message> messageHist = MessageHistory.getHistoryFromBeginning(fsmConfig).complete()
                                .getRetrievedHistory();
                        for (Message message : messageHist) {
                            MessageEmbed embed = message.getEmbeds().get(0);
                            if (embed.getTitle().contains(guild.getName())) {
                                List<Field> fields = embed.getFields();
                                String subChannelId = fields.get(0).getValue().replaceAll("[<@&#>]", "");
                                String subRoleId = fields.get(1).getValue().replaceAll("[<@&#>]", "");
                                String JSONinput = fields.get(6).getValue();
                                SheetConfig sheetConfig = new SheetConfig();
                                sheetConfig = new Gson().fromJson(JSONinput, sheetConfig.getClass());
                                Server s = makeGuild(guild.getId(), subChannelId, subRoleId, sheetConfig);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        guild.getOwner().getUser().openPrivateChannel().queue((res) -> {
                            res.sendMessage("i cannot access config channel :<").queue();
                        });
                        ;
                        // TODO: handle exception
                    }
                } else {
                    guild.getOwner().getUser().openPrivateChannel().queue((res) -> {
                        res.sendMessage(
                                "hello! thank you for inviting FSM to your server! First of all you will want to do /initialize in your server to get things going!")
                                .queue();
                        res.sendMessage("Additionally, here is the manual on how I work :)").queue();
                        res.sendFiles(FileUpload.fromData(new File("FSM Bot user Manual.pdf"))).queue();
                    });
                    guild.upsertCommand(
                            Commands.slash("initialize", "first command to run!")
                                    .addOption(OptionType.CHANNEL, "subchannel", "Substitute Request Channel")
                                    .addOption(OptionType.ROLE, "subrole", "General substitute role for the server")
                                    .setDefaultPermissions(
                                            DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                            .queue();
                }
            }
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

    public synchronized Server makeGuild(Guild guild, MessageChannel subChannel, Role subRole) {
        SheetConfig config = new SheetConfig();
        Server s = new Server(guild, subChannel, subRole, config);
        addListener(s);
        return s;
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
        if (s.hasTeam(name))
            return null;

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
        System.out.println("[" + t.getName() + "]: Finding existing scrims for ");
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
            // finally {
            // if (event != null)
            // event.updateEventMessage(this, true);
            // }
        }
        System.out.println("[" + t.getName() + "]:finished finding scrims");
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
        System.out.println("[" + c.getName() + "]: finding existing sub requests");
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

                            // TODO: replace the existing req for same role in the event.
                            // flag: null message
                            // when checking, check to see if any reqs still have null messages
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
        System.out.println("[" + c.getName() + "]:finished");

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
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println(event.getGuild().getName());
        Guild g = event.getGuild();
        g.getOwner().getUser().openPrivateChannel().queue((res) -> {
            res.sendMessage(
                    "hello! thank you for inviting FSM to your server! First of all you will want to do /initialize in your server to get things going!")
                    .queue();
            res.sendMessage("Additionally, here is the manual on how I work :)").queue();
            res.sendFiles(FileUpload.fromData(new File("FSM Bot user Manual.pdf"))).queue();
        });
        g.upsertCommand(
                Commands.slash("initialize", "first command to run!")
                        .addOption(OptionType.CHANNEL, "subchannel", "Substitute Request Channel")
                        .addOption(OptionType.ROLE, "subrole", "General substitute role for the server")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                .queue();
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

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent slashEvent) {
        if (slashEvent.getName().equalsIgnoreCase("initialize")) {
            MessageChannel subChannel = slashEvent.getOption("subchannel").getAsChannel().asTextChannel();
            Role subRole = slashEvent.getOption("subrole").getAsRole();
            makeGuild(slashEvent.getGuild(), subChannel, subRole);
            slashEvent.reply(
                    "initialized server, now do /makeconfig channel to see the details of your server and /newteam to start creating teams!")
                    .setEphemeral(true).queue();
        }
    }

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
