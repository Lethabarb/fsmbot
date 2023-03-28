package FSM.services;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import FSM.entities.Event;
import FSM.entities.Player;
import FSM.entities.Server;
import FSM.entities.SubRequest;
import FSM.entities.Team;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;


public class DiscordBot extends ListenerAdapter {
    private static JDA bot;
    private static DiscordBot instance;

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
        }
        // logout();
    }

    

    public synchronized static DiscordBot getInstance(String... token) {
        if (instance == null) {
            instance = new DiscordBot(token);
        }
        return instance;
    }

    public synchronized Server makeGuild(String guildId, String subChannelId, String subRoleId) {
        Guild guild = bot.getGuildById(guildId);
        MessageChannel subChannel = guild.getTextChannelById(subChannelId);
        Role subRole = guild.getRoleById(subRoleId);
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Role tankRole = guild.getRoleById(tankRoleId);
        // Role dpsRole = guild.getRoleById(dpsRoleId);
        // Role suppRole = guild.getRoleById(suppRoleId);
        return new Server(guild, subChannel, subRole);
    }

    public synchronized Team makeTeam(String name, String nameAbbv, String minRank, String timetableId,
            String rosterRoleId,
            String trialRoleId, String subRoleId, Server s, int subCalenderId) {
        MessageChannel timetable = bot.getTextChannelById(timetableId);
        Role rosterRole = bot.getRoleById(rosterRoleId);
        Role subRole = bot.getRoleById(subRoleId);
        Role trialRole = bot.getRoleById(trialRoleId);
        List<Member> mems = getMemberOfRole(s.getGuild(), trialRole, rosterRole);
        Team t = new Team(name, nameAbbv, minRank, timetable, rosterRole, trialRole, subRole, mems, subCalenderId);
        // try {
        // // Thread.sleep(5000);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        t.setServer(s);
        return t;
    }

    public synchronized void updateScrims(Team t) {
        System.out.println("updating scrims for " + t.getName());
        try {
            updateAllEvents();
            GoogleSheet sheet = new GoogleSheet();
            LinkedList<Event> events = sheet.getEvents(t.getNameAbbv(), t);
            for (int i = 0; i < events.size(); i++) {
                sendEvent(events.get(i), true);
            }
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean doesEventExist(Event event, MessageChannel c) {
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        for (Message message : messages) {
            try {
                MessageEmbed embed = message.getEmbeds().get(0);
                String timeFormatted = embed.getFields().get(0).getName();
                Long unix = Long.valueOf(timeFormatted.split(":")[1]);
                if (Long.valueOf(event.getUnix()).compareTo(unix) == 0) {
                    return true;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return false;
    }

    public synchronized void createEventsFromChanel(MessageChannel c, Team t) {
        // System.out.println("=========="+t.getName()+"==========");
        System.out.println("Finding existing scrims for " + t.getName());
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        Event event = null;
        for (Message message : messages) {
            try {
                MessageEmbed embed = message.getEmbeds().get(0);
                String timeFormatted = embed.getFields().get(0).getName();
                // System.out.println(timeFormatted);
                Long unix = Long.valueOf(timeFormatted.split(":")[1]);
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unix),
                        ZoneId.of("+11"));
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
                        String userAt = subString.split(" - ")[0];
                        String roleString = subString.split(" - ")[1];
                        String id = userAt.substring(2, userAt.length() - 1);
                        Player p = Player.getPlayer(t.getServer().getGuild().getMemberById(id));
                        if (p == null) {
                            Member m = t.getServer().getGuild().getMemberById(id);
                            int OWrole = -1;
                            for (Role role : m.getRoles()) {
                                if (OWrole == -1) {
                                    OWrole = Player.roleHash(role.getName());
                                }
                            }
                            p = new Player(m, OWrole);
                        }
                        int r = Player.roleHash(roleString);
                        SubRequest sub = new SubRequest(p, null, r);
                        event.addSub(sub);
                        event.setMessage(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            } finally {
                if (event != null)
                    event.updateScrim();
            }
        }
    }

    public synchronized void sendEvent(Event event, boolean sort) {
        boolean exist = doesEventExist(event, event.getTeam().getTimetable());
        if (!exist) {
            TeamUp calendar = TeamUp.getInstance();
            Boolean addedToCal = calendar.addCalenderEvent(event);
            if (!addedToCal) {
                System.out.println("didnt add to cal");
            }
            String[] types = { "Scrim", "AAOL", "Coaching", "Open Div" };
            MessageCreateBuilder message = new MessageCreateBuilder();
            if (sort) {
                message.addContent(
                        event.getTeam().getRosterRole().getAsMention() +
                                event.getTeam().getTrialRole().getAsMention());
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(event.getTitle());
            embed.setAuthor(types[event.getType()]);
            embed.addField(
                    new Field(String.format("<t:%s:F>", event.getUnix()), "(formatted to **your** timezone)", false));
            embed.addField(getEmoji("1025285207273779270") + " Discord", event.getContact1(), true);
            embed.addField(getEmoji("1025284464097632307") + " Bnet", event.getContact2(), true);
            // System.out.println(event.getUnix());
            embed.addBlankField(false);
            embed.addField(getEmoji("1042385181971058698") + " Confirmed", event.confirmed(), true);
            embed.addField(getEmoji("1042385635610210334") + " Not Responded", event.notResponded(), true);
            embed.addField(getEmoji("1042385172835860490") + " Declined", event.declined(), true);
            embed.addBlankField(false);
            embed.addField("subs", event.subs(), false);
            long eventKey = event.gethashCode();
            embed.setFooter(String.valueOf(eventKey));
            message.addEmbeds(embed.build());
            // System.out.print("send: " + eventKey);
            message.addActionRow(Button.primary(String.format("%s_%s", "ScrimButtYes", eventKey), "Yes"),
                    Button.danger(String.format("%s_%s", "ScrimButtNo", eventKey),
                            "No"));
            MessageChannel c = event.getTeam().getTimetable();
            // c.sendMessage(message.build()).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            c.sendMessage(message.build()).queue((m) -> {
                event.setMessage(m);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (sort) {
                sortChannel(c);
            }
            // System.out.println("sent scrim");
        }
    }

    public void sortChannel(MessageChannel c) {
        List<Message> messages = MessageHistory.getHistoryFromBeginning(c).complete().getRetrievedHistory();
        ArrayList<Event> sorted = new ArrayList<>();
        ArrayList<Event> events = Event.messagesToEvents(messages);

        while (events.size() != 0) {
            Event l = events.get(0);
            LocalDateTime lowest = events.get(0).getDateTime();
            for (int i = 1; i < events.size(); i++) {
                Event e = events.get(i);
                if (e.getDateTime().isBefore(lowest)) {
                    lowest = e.getDateTime();
                    l = e;
                }
            }
            events.remove(l);
            c.deleteMessageById(l.getMessage().getId()).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            sorted.add(l);
        }
        for (Event event : sorted) {
            sendEvent(event, false);
        }
    }

    public boolean hasRosterOrTrialRole(Team team, Member member) {
        String rosterRoleId = team.getRosterRole().getId();
        String trialRoleId = team.getTrialRole().getId();
        List<Role> roles = member.getRoles();
        int c = 0;
        boolean found = false;
        while (!found && c < roles.size()) {
            if (roles.get(c).getId().equals(rosterRoleId) || roles.get(c).getId().equals(trialRoleId)) {
                found = true;
            }
            c++;
        }
        return found;
    }

    public void updateNotResondedList(Event e, Team team) {
        LinkedList<Player> notResponded = e.getNotResponded();
        LinkedList<Player> newLL = new LinkedList<>();
        for (int i = 0; i < notResponded.size(); i++) {
            Member m = team.getServer().getGuild().getMemberById(notResponded.get(i).getUserId());
            if (hasRosterOrTrialRole(team, m)) {
                newLL.add(notResponded.get(i));
            }
        }
        List<Member> members = getMemberOfRole(team.getServer().getGuild(), team.getRosterRole(), team.getTrialRole());
        e.getTeam().setMembers(members);
        for (int i = 0; i < members.size(); i++) {
            Player p = Player.getPlayer(members.get(i));
            if (p == null) {
                Member m = members.get(i);
                int OWrole = -1;
                for (Role role : m.getRoles()) {
                    if (OWrole == -1) {
                        OWrole = Player.roleHash(role.getName());
                    }
                }
                p = new Player(m, OWrole);
            }
            if (!newLL.contains(p) && !e.confirmedContains(p) && !e.declinedContains(p)) {
                newLL.add(p);
            }
        }
        e.setNotResponded(newLL);
    }

    public synchronized void updateEvent(Event event) {
        String[] types = { "Scrim", "AAOL", "Coaching", "Open Divison" };
        if (event.getDateTime().toLocalDate().atStartOfDay().compareTo(LocalDate.now().atStartOfDay()) < 0) {
            event.deleteAllSubs();
            event.getMessage().delete().queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            Event.removeFromRepository(event);
        } else {
            if (!event.isSentAnnouncement()
                    && event.getDateTime().compareTo(LocalDate.now().atStartOfDay().plusHours(24)) < 0) {
                String content = "";
                content += types[event.getType()];
                if (event.getType() == 2)
                    content += " with" + event.getTitle() + "\n";
                else
                    content += " vs " + event.getTitle() + "\n";
                content += "bnet: " + event.getContact2() + "\n";
                content += event.confirmed();
                content += "dm me if there are any issues :>";
                User lethabarb = bot.getUserById("251578157822509057");
                lethabarb.openPrivateChannel().complete().sendMessage(content).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                event.setSentAnnouncement(true);

            }
            MessageEditBuilder message = new MessageEditBuilder();
            message.setContent(
                    event.getTeam().getRosterRole().getAsMention() +
                            event.getTeam().getTrialRole().getAsMention());
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(event.getTitle());
            embed.setAuthor(types[event.getType()]);
            embed.addField(new Field(String.format("<t:%s:F>", event.getUnix()), "your local time", false));
            embed.addField(getEmoji("1025285207273779270") + " Discord", event.getContact1(), true);
            embed.addField(getEmoji("1025284464097632307") + " Bnet", event.getContact2(), true);
            // System.out.println(event.getUnix());
            embed.addBlankField(false);
            embed.addField(getEmoji("1042385181971058698") + " Confirmed", event.confirmed(), true);
            embed.addField(getEmoji("1042385635610210334") + " Not Responded", event.notResponded(), true);
            embed.addField(getEmoji("1042385172835860490") + " Declined", event.declined(), true);
            // embed.addField("Discord", event.getContact1(), true);
            // embed.addField("Bnet", event.getContact2(), true);
            // embed.addBlankField(false);
            // embed.addField("Confirmed", event.confirmed(), true);
            // embed.addField("Not Responded", event.notResponded(), true);
            updateNotResondedList(event, event.getTeam());
            // embed.addField("Declined", event.declined(), true);
            embed.addBlankField(false);
            embed.addField("subs", event.subs(), false);
            long eventKey = event.gethashCode();
            embed.setFooter(String.valueOf(eventKey));
            message.setEmbeds(embed.build());
            message.setActionRow(Button.primary(String.format("%s_%s", "ScrimButtYes", eventKey), "Yes"),
                    Button.danger(String.format("%s_%s", "ScrimButtNo", eventKey),
                            "No"));
            MessageChannel c = event.getTeam().getTimetable();
            event.getMessage().editMessage(message.build()).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public String getEmoji(String id) {
        return bot.getEmojiById(id).getAsMention();
    }

    public synchronized void sendSubRequest(Event event, int role) {
        String tankpng = "https://media.discordapp.net/attachments/740876905313599509/1044014909656137738/tank.png";
        String dpspng = "https://media.discordapp.net/attachments/740876905313599509/1044014917952475156/dps.png";
        String supportpng = "https://media.discordapp.net/attachments/740876905313599509/1044014928094298175/support.png";
        if (event.getType() == 2)
            return; // if its coaching
        MessageCreateBuilder m = new MessageCreateBuilder();
        m.setContent(event.getTeam().getServer().getSubRole().getAsMention());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);

        int width = 14;
        String lineDiv = getEmoji("1043359277441618030");

        embed.setTitle("**LFS** " + String.format("<t:%s:F>", event.getUnix()));
        int remaining = width - event.getTeam().getName().length() / 3;
        if (remaining % 2 == 0) {
            int sides = remaining / 2;
            embed.setDescription(lineDiv.repeat(sides) + event.getTeam().getName() + lineDiv.repeat(sides));
        } else {
            int sides = remaining / 2;
            embed.setDescription(
                    lineDiv.repeat(sides) + "**" + event.getTeam().getName() + "**" + lineDiv.repeat(sides + 1));
        }
        Field rolefield = new Field("```Role```", "Tank", true);
        // Field time = new Field("```Date / Time```", String.format("<t:%s:F>",
        // event.getUnix()), true);

        if (role == Player.TANK) {
            embed.setThumbnail(tankpng);
            rolefield = new Field("```Role```", "Tank", true);
        }
        if (role == Player.DPS) {
            embed.setThumbnail(dpspng);
            rolefield = new Field("```Role```", "DPS", true);
        }
        if (role == Player.SUPPORT) {
            embed.setThumbnail(supportpng);
            rolefield = new Field("```Role```", "Support", true);
        }
        embed.addField(rolefield);
        Field rank = new Field("```Rank```", event.getTeam().getMinRank(), true);
        embed.addField(rank);
        // embed.addField(time);

        m.addEmbeds(embed.build());
        long eventKey = event.gethashCode();
        m.addActionRow(Button.primary(
                String.format("%s_%s_%s_%s", "Sub", eventKey, event.getSubIndex(), role),
                "Sub "));
        MessageChannel c = event.getTeam().getServer().getSubChannel();
        c.sendMessage(m.build()).queue((message) -> {
            event.addSub(new SubRequest(null, message, role));
        });

    }

    public synchronized void deleteSubRequest(Event event, int subIndex) {
        MessageChannel c = event.getTeam().getServer().getSubChannel();
        c.deleteMessageById(event.getSubMessage(subIndex).getId()).queue();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            // TODO: handle exception
        }
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
            // TODO: handle exception
        }
    }

    public synchronized void removeMemberRole(Guild g, Member m, Role r) {
        g.removeRoleFromMember(m, r).queue();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            // TODO: handle exception
        }
        ;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
        System.out.println(buttonEvent.getMember().getUser().getName());
        Button b = buttonEvent.getButton();
        String[] buttonData = b.getId().split("_");
        String buttonUse = buttonData[0];
        long eventKey = Long.parseLong(buttonData[1]);
        // System.out.println("key: " + eventKey);
        Event event = Event.getEvent(eventKey);
        Player trigger = Player.getPlayer(buttonEvent.getMember());

        if (buttonUse.equals("ScrimButtYes")) {
            if (hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
                boolean wasSub = event.addConfirmed(trigger);
                if (wasSub) {
                    try {
                        if (trigger.getRole() == -1) {
                            buttonEvent.reply(
                                    "you dont have a valid overwatch role for this event, please contact your manager");
                            return;
                        }
                        int sub = event.getExistingSub(trigger.getRole(), false);
                        try {
                            deleteSubRequest(event, sub);
                        } catch (Exception e) {
                            System.out.println("no sub message found");
                        }
                        event.removeSub(sub);
                    } catch (Exception e) {

                    }
                }
                if (!event.needsSub(trigger.getRole())) {
                    int sub = event.getExistingSub(trigger.getRole(), false);
                    while (sub != -1) {
                        deleteSubRequest(event, sub);
                        event.removeSub(sub);
                    }
                }
                updateEvent(event);
                buttonEvent.reply("You have accepted the scrim on <t:" + event.getUnix() + ":F>").setEphemeral(true)
                        .queue();
            } else {
                buttonEvent.reply("you are not on the roster!").setEphemeral(true).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } else if (buttonUse.equals("ScrimButtNo")) {
            if (hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
                event.addDeclined(trigger);
                updateEvent(event);
                if (event.needsSub(trigger.getRole()))
                    sendSubRequest(event, trigger.getRole());
                buttonEvent.reply("You have declined the scrim on <t:" + event.getUnix() + ":F>").setEphemeral(true)
                        .queue();
            } else {
                buttonEvent.reply("you are not on the roster!").setEphemeral(true).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } else if (buttonUse.equals("Sub")) {
            int subIndex = Integer.parseInt(buttonData[2]);
            int role = Integer.parseInt(buttonData[3]);
            if (trigger == null) {
                trigger = new Player(buttonEvent.getMember(), role);
            }
            if (!hasRosterOrTrialRole(event.getTeam(), buttonEvent.getMember())) {
                event.setSubPlayer(subIndex, trigger);
                deleteSubRequest(event, subIndex);
                giveMemberRole(event.getTeam().getServer().getGuild(), buttonEvent.getMember(),
                        event.getTeam().getSubRole());
                buttonEvent.reply("you are now subbing").setEphemeral(true).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                updateEvent(event);
            } else {
                buttonEvent.reply("you are on the roster! how can u be a sub??").setEphemeral(true).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    public synchronized void updateAllEvents() {
        LinkedList<Event> events = Event.getAllEvents();
        for (Event event : events) {
            updateEvent(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent commandEvent) {
        String command = commandEvent.getName();
        System.out.println(commandEvent.getMember().getUser().getName());
        if (command.equals("update")) {
            String subCommand = commandEvent.getSubcommandName();
            if (subCommand.equals("events")) {
                System.out.println(commandEvent.getMember().getUser().getName() + " called /update events");
                updateAllEvents();
                commandEvent.reply("events have been updated").setEphemeral(true).queue();
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } else if (command.equals("role")) {
            Server s = Server.getGuild(commandEvent.getGuild().getIdLong());
            String roleName = commandEvent.getOption("newplayerrole").getAsRole().getName();
            Member member = commandEvent.getOption("playerdiscord").getAsMember();
            Role role = commandEvent.getOption("newplayerrole").getAsRole();
            changeRoles(member, s.getGuild());
            s.getGuild().addRoleToMember(member, role);
            Player p = Player.getPlayer(member);
            p.setRole(Player.roleHash(roleName));
            updateAllEvents();
        } else if (command.equals("sort")) {
            MessageChannel c = commandEvent.getMessageChannel();
            sortChannel(c);
        }
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent context) {
        Message message = context.getTarget();
        System.out.println(context.getMember().getUser().getName());
        if (message.getAuthor().getName().equals(bot.getSelfUser().getName())) {
            MessageEmbed embed = message.getEmbeds().get(0);
            Event event = Event.getEvent(Long.parseLong(embed.getFooter().getText()));

            TextInput dateTime = TextInput.create("datetime", "event date + time", TextInputStyle.SHORT)
                    .setValue(event.getDateTime().format(DateTimeFormatter.ofPattern("E dd/MM/yyyy h:mm a", Locale.US)))
                    .build();
            TextInput eventhash = TextInput.create("hash", "hash (DONT EDIT)", TextInputStyle.SHORT)
                    .setValue(String.valueOf(event.gethashCode()))
                    .build();
            TextInput bnetPoc = TextInput.create("bnet", "bnet", TextInputStyle.SHORT)
                    .setValue(event.getContact2()).build();
            TextInput discPoc = TextInput.create("disc", "disc", TextInputStyle.SHORT)
                    .setValue(event.getContact1()).build();

            TextInput confirmed;
            try {
                confirmed = TextInput.create("confirmed", "confirmed", TextInputStyle.PARAGRAPH)
                        .setValue(event.confirmedString())
                        .build();
            } catch (Exception e) {
                confirmed = TextInput.create("confirmed", "confirmed", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("confirmed players")
                        .build();
            }
            TextInput NR;
            try {
                NR = TextInput.create("notresponded", "not responded", TextInputStyle.PARAGRAPH)
                        .setValue(event.notRespondedString())
                        .build();
            } catch (Exception e) {
                NR = TextInput.create("notresponded", "not responded", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("not responded players")
                        .build();
            }
            TextInput declined;
            try {
                declined = TextInput.create("declined", "declined", TextInputStyle.PARAGRAPH)
                        .setValue(event.declinedString())
                        .build();
            } catch (Exception e) {
                declined = TextInput.create("declined", "declined", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("declined players")
                        .build();
            }

            Modal modal = Modal.create("eventedit", "Event Edit: " + event.gethashCode())
                    .addActionRows(ActionRow.of(eventhash), ActionRow.of(dateTime), ActionRow.of(confirmed),
                            ActionRow.of(NR),
                            ActionRow.of(declined))
                    .build();

            context.replyModal(modal).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }

        } else {
            context.reply("this isnt an event message").setEphemeral(true).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        System.out.println(event.getMember().getUser().getName());

        if (event.getModalId().equals("eventedit")) {
            String dateTimeString = event.getValue("datetime").getAsString();
            String confirmedString = event.getValue("confirmed").getAsString();
            String notRespondedString = event.getValue("notresponded").getAsString();
            String declinedString = event.getValue("declined").getAsString();
            String eventHash = event.getValue("hash").getAsString();
            String bnet = event.getValue("bnet").getAsString();
            String disc = event.getValue("disc").getAsString();

            Event scrim = Event.getEvent(Long.valueOf(eventHash));
            Event.removeFromRepository(scrim);
            LocalDateTime datetime = LocalDateTime.parse(dateTimeString,
                    DateTimeFormatter.ofPattern("E dd/MM/yyyy h:mm a", Locale.US));
            scrim.setDateTime(datetime);
            scrim.setContact1(disc);
            scrim.setContact2(bnet);

            // confirmedString.replaceAll(" ", "");
            // notRespondedString.replaceAll(" ", "");
            // declinedString.replaceAll(" ", "");
            for (String s : notRespondedString.split(" ")) {
                Member member = null;
                for (Member m : scrim.getTeam().getMembers()) {
                    System.out.println(String.format("'%s' == '%s'", s, m.getUser().getName()));
                    if (m.getUser().getName().equals(s.trim())) {
                        System.out.print("set member");
                        member = m;
                    }
                }
                if (member != null) {
                    Player p = Player.getPlayer(member);
                    if (p != null) {
                        scrim.addNR(p);
                    } else {
                        System.out.println("p is null 614");
                    }
                } else {
                    System.out.println("m is null 617");
                }
            }

            for (String s : confirmedString.split(" ")) {
                Member member = null;
                for (Member m : scrim.getTeam().getMembers()) {
                    if (m.getUser().getName().equals(s.trim())) {
                        member = m;
                    }
                }
                if (member != null) {
                    Player p = Player.getPlayer(member);
                    if (p != null) {
                        scrim.addConfirmed(p);
                    } else {
                        System.out.println("p is null 569");
                    }
                } else {
                    System.out.println("m is null 599");
                }
            }
            for (String s : declinedString.split(" ")) {
                Member member = null;
                for (Member m : scrim.getTeam().getMembers()) {
                    if (m.getUser().getName().equals(s.trim())) {
                        member = m;
                    }
                }
                if (member != null) {
                    Player p = Player.getPlayer(member);
                    if (p != null) {
                        scrim.addDeclined(p);
                    } else {
                        System.out.println("p is null 632");
                    }
                } else {
                    System.out.println("m is null 635");
                }
            }
            System.out.print(scrim.notRespondedString());

            Event.addEvent(scrim.gethashCode(), scrim);
            updateEvent(scrim);
            GoogleSheet sheet = new GoogleSheet();
            sheet.updateEvent(scrim.getTeam().getNameAbbv(), scrim);
            event.reply("Thanks for your request!").setEphemeral(true).queue();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                // TODO: handle exception
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
}
