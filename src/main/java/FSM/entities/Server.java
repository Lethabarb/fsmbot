package FSM.entities;

import java.io.File;
import java.lang.StackWalker.Option;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.swing.Action;

import com.google.common.base.Predicate;
import com.google.gson.Gson;

import FSM.entities.EventJobs.SendManagerMessage;
import FSM.services.DiscordBot;
import FSM.services.EventJobRunner;
import FSM.services.GoogleSheet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.component.ModalImpl;

public class Server extends ListenerAdapter implements Runnable {
    private static HashMap<Long, Server> repoos = new HashMap<>();
    private static boolean running = false;
    private static final String BRONZE_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460758862057563/bronze-t.png";
    private static final String SILVER_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460768894816348/silver-t.png";
    private static final String GOLD_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460777019191307/gold-t.png";
    private static final String PLAT_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460784715743292/plat-t.png";
    private static final String DIAMOND_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460792139644978/dia-t.png";
    private static final String MASTER_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131461925360893992/master-t.png";
    private static final String GM_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460800939294750/gm-t.png";
    private static final String T500_ICON = "https://media.discordapp.net/attachments/1102804706578944001/1131460809147551785/t500-t.png";
    private HashMap<String, Team> teams = new HashMap<>();
    private Team soloTeam = null;
    private net.dv8tion.jda.api.entities.Guild guild;
    private MessageChannel subChannel;
    private Role subRole = null;
    private MessageChannel botConfigChannel;
    private boolean differentTeamSheetSetups = false;
    private GoogleSheet sheet;
    private SheetConfig sheetConfig;
    private Thread t;
    private InteractionHook configEditMessage = null;
    private HashMap<String, ActionRow> editFields = new HashMap<>();
    // private MessageCreateBuilder configEditBuilder = null;

    // public Server(Guild guild) {
    // List<GuildChannel> channels = guild.getChannels();
    // for (GuildChannel guildChannel : channels) {
    // if (guildChannel.getName().equalsIgnoreCase("fsm-config")) {
    // botConfigChannel = guild.getTextChannelById(guildChannel.getId());
    // }
    // }

    // }

    // public static void main(String[] args) {
    // HashMap<Integer, String> hash = new HashMap<>();
    // hash.put(1, "1");
    // hash.put(2, "2");
    // hash.put(3, "3");
    // hash.put(4, "4");
    // hash.put(5, "5");
    // System.out.println(hash.size());
    // }

    public static synchronized boolean isRunning() {
        return running;
    }

    // public static synchronized void changeRunning() {
    // running = !running;
    // }
    public static synchronized void stopRunning() {
        running = false;
    }

    public static synchronized void startRunning() {
        running = true;
    }

    public Server(Guild guild, MessageChannel subChannel, Role subRole, SheetConfig sheetConfig) {
        System.out.println("Creating " + guild.getName() + " Server");
        this.guild = guild;
        this.subChannel = subChannel;
        this.subRole = subRole;
        this.sheetConfig = sheetConfig;
        System.out.println("adding commands...");

        guild.updateCommands().addCommands(
                Commands.slash("makeconfigchannel",
                        "sets the current channel for the guild to the bot config channel")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("updateevents", "re-freshes an event details")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("role", "edit role of a player")
                        .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                        .addOption(OptionType.ROLE, "newplayerrole", "role to make the player")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("sort", "sort the events of a channel")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("removesubs", "removes the subs of a given team")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("help", "request the bot's manual")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.context(Type.MESSAGE, "edit responses")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.context(Type.MESSAGE, "edit details")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.context(Type.MESSAGE, "delete event")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("newteam", "create a new team")
                        .addOption(OptionType.STRING, "name", "name of the team", true, false)
                        .addOption(OptionType.STRING, "abbv", "abbreviation of the name, for sheet config",
                                true, false)
                        .addOption(OptionType.STRING, "minrank", "min rank for subs", true, false)
                        .addOption(OptionType.CHANNEL, "timetable", "timetable / schedule channel",
                                true, false)
                        .addOption(OptionType.CHANNEL, "announcement", "announcement channel", true,
                                false)
                        .addOption(OptionType.ROLE, "roster", "roster role", true, false)
                        .addOption(OptionType.ROLE, "trial", "trial role", true, false)
                        .addOption(OptionType.ROLE, "sub", "sub role", true, false)
                        .addOption(OptionType.USER, "manager", "team manager", true, false)
                        .addOption(OptionType.USER, "coach", "team coach", true, false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("rosters",
                        "create a set of messages containing the list of teams and players in your server")
                        .addOption(OptionType.CHANNEL, "rosterchannel", "channel to send team list to", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                .queue();
        // soloTeam = teams[0];

        // }

        System.out.println("added commands");
        // List<GuildChannel> channels = guild.getChannels();
        // for (GuildChannel guildChannel : channels) {
        // if (guildChannel.getName().equalsIgnoreCase("fsm-config"));
        // }
        // for (Team t : teams) {
        // this.teams.put(t.getName(), t);
        // }
        try {
            repoos.put(guild.getIdLong(), this);
            this.t = new Thread(this, guild.getName());
            this.t.start();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    @Override
    public void run() {
        System.out.println(String.format("[%s] Starting thread", guild.getName()));
        DiscordBot bot = DiscordBot.getInstance();
        while (isRunning()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startRunning();
        System.out.println(String.format("[%s] running init", guild.getName()));
        // find config and load

        System.out.println(String.format("[%s]finding config channel", guild.getName()));
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase("fsm-config")) {
                botConfigChannel = channel;
            }
        }
        if (botConfigChannel != null) {
            System.out.println(String.format("[%s]collecting data", guild.getName()));
            List<Message> messageHist = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete()
                    .getRetrievedHistory();

            for (Message message : messageHist) {
                if (!message.getEmbeds().get(0).getTitle().contains(guild.getName())) {
                    MessageEmbed embed = message.getEmbeds().get(0);
                    String fullTitle = embed.getTitle();
                    String[] titleData = { "", "" };
                    try {
                        titleData = fullTitle.split("[\\(\\)]");
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                    List<Field> fields = embed.getFields();

                    // String name = "titleData[0]";
                    // String abbv = "titleData[1].replaceAll(\"()\", \"\")";
                    if (fields.get(2).getName().equalsIgnoreCase("manager")) {
                        String name = titleData[0].trim();
                        String abbv = titleData[1].replaceAll("[\\(\\)]", "");
                        String minRank = fields.get(0).getValue();
                        String managerId = fields.get(2).getValue().replaceAll("[<@&#>]", "");
                        String timetableId = fields.get(4).getValue().replaceAll("[<@&#>]", "");
                        String announceId = fields.get(5).getValue().replaceAll("[<@&#>]", "");
                        String rosterId = fields.get(7).getValue().replaceAll("[<@&#>]", "");
                        String trialId = fields.get(8).getValue().replaceAll("[<@&#>]", "");
                        String subId = fields.get(9).getValue().replaceAll("[<@&#>]", "");

                        Team t = bot.makeTeam(name, abbv, minRank, timetableId, announceId, rosterId, trialId, subId,
                                this,
                                0, subId, managerId);
                        if (t == null) {
                            System.out.println("team already exists");
                        }
                    } else {
                        String name = titleData[0].trim();
                        String abbv = titleData[1].replaceAll("[\\(\\)]", "");
                        String minRank = fields.get(0).getValue();
                        String managerId = fields.get(3).getValue().replaceAll("[<@&#>]", "");
                        String coachId = fields.get(4).getValue().replaceAll("[<@&#>]", "");
                        String timetableId = fields.get(6).getValue().replaceAll("[<@&#>]", "");
                        String announceId = fields.get(7).getValue().replaceAll("[<@&#>]", "");
                        String rosterId = fields.get(9).getValue().replaceAll("[<@&#>]", "");
                        String trialId = fields.get(10).getValue().replaceAll("[<@&#>]", "");
                        String subId = fields.get(11).getValue().replaceAll("[<@&#>]", "");

                        Team t = bot.makeTeam(name, abbv, minRank, timetableId, announceId, rosterId, trialId, subId,
                                this,
                                0, subId, managerId, coachId);
                        if (t == null) {
                            System.out.println("team already exists");
                        }
                    }
                }
            }
            System.out.println(String.format("[%s] updating config channel", guild.getName()));

            if (messageHist.size() > 0)
                botConfigChannel.purgeMessages(messageHist);
            botConfigChannel.sendMessage(createConfig()).queue();
            for (Team team : teams.values()) {
                botConfigChannel.sendMessage(team.createConfig()).queue();
            }
        }

        for (Team team : teams.values()) {
            bot.createEventsFromChanel(team);
            bot.updateAllEvents(team);
        }
        bot.createSubReqestsFromChannel(subChannel);
        for (Team team : teams.values()) {
            team.checkEventSubRequests();
            ZonedDateTime dt = LocalDate.now().atStartOfDay(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
            SendManagerMessage job = new SendManagerMessage(team, dt);
            EventJobRunner.getInstance().addJob(job);
        }
        stopRunning();
        while (true) {
            while (isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            startRunning();
            // ArrayList<Team> teamsList = new ArrayList<>(teams.values());
            for (Team team : teams.values()) {
                bot.updateScrims(team);
                while (bot.getQueue() > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                bot.sortChannel(team.getTimetable());
            }
            stopRunning();
            try {
                Thread.sleep(2 * 60 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent slashCommand) {
        if (!slashCommand.getGuild().getId().equalsIgnoreCase(this.guild.getId()))
            return;
        String command = slashCommand.getName();
        if (command.equalsIgnoreCase("makeconfigchannel")) {
            if (botConfigChannel != null
                    && slashCommand.getChannel().getId().equalsIgnoreCase(botConfigChannel.getId())) {
                slashCommand.reply("cannot use this command in the config channel :>").setEphemeral(true).queue();
            }
            InteractionHook reply = slashCommand.deferReply(true).complete();
            try {
                reply.editOriginal("finding configChannel").queue();
                if (botConfigChannel == null) {
                    reply.editOriginal("no recorded channel, searching...").queue();
                    for (TextChannel channel : guild.getTextChannels()) {
                        if (channel.getName().equalsIgnoreCase("fsm-config")) {
                            botConfigChannel = channel;
                            reply.editOriginal("found channel").queue();
                        }
                    }
                    if (botConfigChannel == null) {
                        reply.editOriginal("no channel found, creating").queue();
                        botConfigChannel = guild.createTextChannel("fsm-config", guild.getCategories().get(0))
                                .complete();
                    }
                }
                List<Message> messageHist = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete()
                        .getRetrievedHistory();
                if (messageHist.size() > 0)
                    botConfigChannel.purgeMessages(messageHist);
                reply.editOriginal("sending message for server").queue();
                botConfigChannel.sendMessage(createConfig()).queue();
                for (Team team : teams.values()) {
                    reply.editOriginal("sending message for " + team.getName()).queue();
                    botConfigChannel.sendMessage(team.createConfig()).queue();
                }
            } catch (Exception e) {
                reply.editOriginal(e.getMessage()).queue();
            }
            reply.editOriginal("finished").queue();
            reply.deleteOriginal().queue();

        } else if (command.equalsIgnoreCase("updateevents")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            try {
                reply.editOriginal("finding team").queue();
                if (teams.size() > 1) {
                    Role rosterRole = slashCommand.getOption("team").getAsRole();
                    for (Team t : teams.values()) {
                        if (t.getRosterRole().getId().equalsIgnoreCase(rosterRole.getId())) {
                            reply.editOriginal("found, updating").queue();
                            DiscordBot.getInstance().updateAllEvents(t);
                            reply.editOriginal("finished").queue();
                            reply.deleteOriginal().queue();
                        }
                    }
                } else {
                    reply.editOriginal("found, updating").queue();
                    DiscordBot.getInstance().updateAllEvents(soloTeam);
                    reply.editOriginal("finished").queue();
                    reply.deleteOriginal().queue();

                }
            } catch (Exception e) {
                e.printStackTrace();
                reply.editOriginal(e.getMessage()).queue();
                // TODO: handle exception
            } finally {

            }
        } else if (command.equalsIgnoreCase("Role")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            reply.editOriginal("removing role").queue();
            Member member = slashCommand.getOption("playerdiscord").getAsMember();
            Role newRole = slashCommand.getOption("newplayerrole").getAsRole();
            List<Role> memberRoles = member.getRoles();
            int count = 0;
            String oldRole = "";
            while (Player.roleHash(memberRoles.get(count).getName()) == -1) {
                count++;
            }
            oldRole = memberRoles.get(count).getName();
            reply.editOriginal("giving role").queue();
            guild.removeRoleFromMember(member, memberRoles.get(count)).queue((res) -> {
                System.out.println("removed role ");
                reply.editOriginal("success");
                reply.deleteOriginal().queue();
            }, (res) -> {
                reply.editOriginal("error");
                System.out.println("could not remove role");
            });
            guild.addRoleToMember(member, newRole).queue();

        } else if (command.equalsIgnoreCase("removesubs")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            reply.editOriginal("processing").queue();
            for (Team t : teams.values()) {
                reply.editOriginal("removing for: " + t.getName()).queue();
                try {
                    List<Member> membersWithSubRole = guild.getMembersWithRoles(t.getSubRole());
                    for (Member member : membersWithSubRole) {
                        guild.removeRoleFromMember(member, t.getSubRole()).queue();
                    }
                } catch (Exception e) {
                    reply.editOriginal(e.getMessage()).queue();
                }
                reply.editOriginal("finished").queue();
                reply.deleteOriginal().queue();

            }
        } else if (command.equalsIgnoreCase("sort")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            reply.editOriginal("sorting...").queue();
            ;
            try {
                DiscordBot.getInstance().sortChannel(slashCommand.getChannel());
                reply.editOriginal("finished").queue();
                reply.deleteOriginal().queue();
            } catch (Exception e) {
                reply.editOriginal(e.getMessage()).queue();
                // TODO: handle exception
            }
        } else if (command.equalsIgnoreCase("help")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            reply.editOriginal("sending manual").queue();
            slashCommand.getUser().openPrivateChannel().queue((res) -> {
                res.sendFiles(FileUpload.fromData(new File("FSM Bot user Manual.pdf"))).queue();
                reply.editOriginal("sent");
                reply.deleteOriginal().queue();
            }, (res) -> {
                reply.editOriginal("error opening private channel").queue();
            });
        } else if (command.equalsIgnoreCase("newteam")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            reply.editOriginal("creating...").queue();

            String name = slashCommand.getOption("name").getAsString();
            String abbv = slashCommand.getOption("abbv").getAsString();
            String minRank = slashCommand.getOption("minrank").getAsString();
            MessageChannel timetable = slashCommand.getOption("timetable").getAsChannel().asTextChannel();
            MessageChannel announcement = slashCommand.getOption("announcement").getAsChannel().asTextChannel();
            Role roster = slashCommand.getOption("roster").getAsRole();
            Role trial = slashCommand.getOption("trial").getAsRole();
            Role sub = slashCommand.getOption("sub").getAsRole();
            User manager = slashCommand.getOption("manager").getAsUser();
            User coach = slashCommand.getOption("coach").getAsUser();
            Team t = new Team(name, abbv, minRank, timetable, announcement, roster, trial, sub, manager, coach);
            teams.put(name, t);
            DiscordBot.getInstance().addListener(t);
            t.setGuild(this);

            reply.editOriginal("updating config...").queue();
            if (botConfigChannel != null) {
                List<Message> messageHist = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete()
                        .getRetrievedHistory();
                if (messageHist.size() > 0)
                    botConfigChannel.purgeMessages(messageHist);
                botConfigChannel.sendMessage(createConfig()).queue();
                for (Team team : teams.values()) {
                    botConfigChannel.sendMessage(team.createConfig()).queue();
                }

            }
            reply.editOriginal("complete").queue();
            reply.deleteOriginal().queue();

        } else if (command.equalsIgnoreCase("rosters")) {
            MessageChannel c = slashCommand.getOption("rosterchannel").getAsChannel().asTextChannel();
            for (Team t : teams.values())
                t.sendRosterMessage(c);
            slashCommand.reply("complete").setEphemeral(true).queue();
        }
    }

    public String getRankIcon(Team t) {
        String minRank = t.getMinRank();
        if (minRank.contains("Bronze"))
            return BRONZE_ICON;
        if (minRank.contains("Silver"))
            return SILVER_ICON;
        if (minRank.contains("Gold"))
            return GOLD_ICON;
        if (minRank.contains("Plat"))
            return PLAT_ICON;
        if (minRank.contains("Diamond"))
            return DIAMOND_ICON;
        if (minRank.contains("Master"))
            return MASTER_ICON;
        if (minRank.contains("Grand"))
            return GM_ICON;
        if (minRank.contains("T500"))
            return T500_ICON;
        return "";
    }

    public synchronized void createEditingMessage(ButtonInteractionEvent event) {
        if (configEditMessage != null)
            configEditMessage.deleteOriginal().queue();
        configEditMessage = event.deferReply(true).complete();
        configEditMessage.editOriginalComponents(editFields.values()).queue();
    }

    public synchronized void createEditingMessage() {
        if (editFields.size() == 0) {
            configEditMessage.deleteOriginal().queue();
        } else {
            configEditMessage.editOriginalComponents(editFields.values()).queue();
        }
    }

    public synchronized void addEditingComponent(ActionRow row, ButtonInteractionEvent event) {
        if (editFields.size() < 5) {
            editFields.put(row.getActionComponents().get(0).getId(), row);
            createEditingMessage(event);
        } else {
            event.reply("cannot have more than 5 menus").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
        String[] data = buttonEvent.getButton().getId().split("_");
        if (!data[0].equalsIgnoreCase(guild.getName()))
            return;
        if (botConfigChannel == null) {
            for (TextChannel channel : guild.getTextChannels()) {
                if (channel.getName().equalsIgnoreCase("fsm-config")) {
                    botConfigChannel = channel;
                }
            }
        }
        if (botConfigChannel == null) {
            buttonEvent.reply("no config (strange)").setEphemeral(true).queue();
            return;
        }
        String buttonUse = data[1];
        if (buttonUse.equalsIgnoreCase("editSubChannel")) {
            // SelectMenu channels = SelectMenu.create("channelsselectmenu")
            // guild.getTextChannels()
            // configEditBuilder.addActionRow(channelSelectMenu);
            // configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            // createEditingMessage(buttonEvent);
            SelectMenu channelSelectMenu = getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("sub"), "subChannelEdit");
            addEditingComponent(ActionRow.of(channelSelectMenu), buttonEvent);
            createEditingMessage(buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editSubRole")) {
            // createEditingMessage();
            // configEditBuilder.addActionRow(channelSelectMenu);
            // configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            SelectMenu channelSelectMenu = getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("sub"),
                    "subRoleEdit");
            addEditingComponent(ActionRow.of(channelSelectMenu), buttonEvent);
            createEditingMessage(buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editCoinfigChannel")) {
            // createEditingMessage();
            // configEditBuilder.addActionRow(channelSelectMenu);
            // configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            // createEditingMessage();
            SelectMenu channelSelectMenu = getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("config"), "configChannelEdit");
            addEditingComponent(ActionRow.of(channelSelectMenu), buttonEvent);
            createEditingMessage(buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("toggleDifferentSheets")) {
            differentTeamSheetSetups = !differentTeamSheetSetups;
            for (Team t : teams.values()) {
                if (differentTeamSheetSetups) {
                    try {
                        t.setSheetConfig((SheetConfig) sheetConfig.clone());
                        t.updateConfigMessage();
                    } catch (CloneNotSupportedException e) {
                        System.out.println("cannot clone");
                        e.printStackTrace();
                    }
                } else {
                    t.setSheetConfig(null);
                    t.updateConfigMessage();
                }
            }
            updateConfigMessage();
            buttonEvent.deferReply(true).complete().deleteOriginal().queue();
        } else if (buttonUse.equalsIgnoreCase("editConfigJSON")) {
            TextInput textfield = TextInput.create("configJsonTextInput", "config JSON", TextInputStyle.PARAGRAPH)
                    .setValue(sheetConfig.toJSON()).build();
            buttonEvent
                    .replyModal(
                            Modal.create(guild.getName() + "_editConfigJSON", "edit sheet config JSON")
                                    .addActionRow(textfield).build())
                    .queue();
        }
        // else if (buttonUse.equalsIgnoreCase("submitConfigEdit")) {
        // configEditBuilder = null;
        // configEditMessage.delete().queue();
        // configEditMessage = null;
        // }
    }

    public void updateConfigMessage() {
        List<Message> messageHistory = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete()
                .getRetrievedHistory();
        for (Message message : messageHistory) {
            try {
                if (message.getEmbeds().get(0).getTitle().equalsIgnoreCase(guild.getName() + " config")) {
                    message.editMessage(MessageEditData.fromCreateData(createConfig())).queue();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    // TODO: fix add team command updates, /update to update rosters properly

    @Override
    public void onSelectMenuInteraction(@Nonnull SelectMenuInteractionEvent event) {
        String[] eventData = event.getSelectMenu().getId().split("_");
        if (!eventData[0].equalsIgnoreCase(guild.getName()))
            return;

        InteractionHook reply = event.deferReply(true).complete();
        reply.editOriginal("editing").queue();
        // int len = event.getSelectMenu().getId().split("_").length;
        String use = eventData[1];
        String value = event.getValues().get(0);
        // String teamName = "";
        // Team team = teams.get(teamName);
        if (use.equalsIgnoreCase("subChannelEdit")) {
            subChannel = guild.getTextChannelById(value);
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("subRoleEdit")) {
            subRole = guild.getRoleById(value);
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("configChannelEdit")) {
            botConfigChannel = guild.getTextChannelById(value);
            updateConfigMessage();
        }
        // else if (use.equalsIgnoreCase("managerSelect")) {
        // team.setManager(guild.getMemberById(value).getUser());
        // team.updateConfigMessage();
        // } else if (use.equalsIgnoreCase("timetableEdit")) {
        // team.setTimetable(guild.getTextChannelById(value));
        // team.updateConfigMessage();
        // } else if (use.equalsIgnoreCase("announceEdit")) {
        // team.setAnnouncement(guild.getTextChannelById(value));
        // team.updateConfigMessage();
        // } else if (use.equalsIgnoreCase("rosterEdit")) {
        // team.setRosterRole(guild.getRoleById(value));
        // team.updateConfigMessage();
        // } else if (use.equalsIgnoreCase("trialEdit")) {
        // team.setTrialRole(guild.getRoleById(value));
        // team.updateConfigMessage();
        // } else if (use.equalsIgnoreCase("subEdit")) {
        // team.setSubRole(guild.getRoleById(value));
        // team.updateConfigMessage();
        // }

        // for (ActionRow row : editFields) {
        // if
        // (row.getActionComponents().get(0).getId().equalsIgnoreCase(event.getSelectMenu().getId()))
        // {

        // }
        // }
        reply.deleteOriginal().queue();
        removeActionRow(event.getSelectMenu().getId());
        createEditingMessage();
    }

    public synchronized void removeActionRow(String selectMenuId) {
        for (int i = 0; i < editFields.size(); i++) {
            editFields.remove(selectMenuId);
        }
        createEditingMessage();

    }

    @Override
    public void onModalInteraction(ModalInteractionEvent modalEvent) {
        String[] data = modalEvent.getModalId().split("_");
        if (!data[0].equalsIgnoreCase(guild.getName()))
            return;

        ModalMapping channelId = modalEvent.getValue("channelselectmenu");
        ModalMapping roleId = modalEvent.getValue("roleselectmenu");
        ModalMapping JSONinput = modalEvent.getValue("configJsonTextInput");
        try {
            if (channelId != null && modalEvent.getModalId().equalsIgnoreCase("editsubchannel"))
                subChannel = guild.getTextChannelById(channelId.getAsString());
            if (channelId != null && modalEvent.getModalId().equalsIgnoreCase("editconfigchannel"))
                botConfigChannel = guild.getTextChannelById(channelId.getAsString());
            if (roleId != null)
                subRole = guild.getRoleById(roleId.getAsString());
            if (JSONinput != null)
                sheetConfig = new Gson().fromJson(JSONinput.getAsString(), sheetConfig.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            modalEvent.reply(e.getMessage()).setEphemeral(true).queue();
        }
        updateConfigMessage();
        modalEvent.reply("updated").setEphemeral(true).queue();
    }

    public SelectMenu getChannelSelectMenu(Predicate<TextChannel> pred, String id) {
        LinkedList<TextChannel> channels = new LinkedList<>(guild.getTextChannels());
        channels.removeIf(pred);
        LinkedList<SelectOption> channelOptions = new LinkedList<>();

        for (TextChannel channel : channels) {
            String parent = "no catagory";
            try {
                parent = channel.getParentCategory().getName();
            } catch (Exception e) {
                // no catagory
            }
            SelectOption opt = SelectOption.of(parent + " : " + channel.getName(),
                    channel.getId());
            channelOptions.add(opt);
        }

        SelectMenu channelSelectMenu = SelectMenu.create(guild.getName() + "_" + id).addOptions(channelOptions)
                .setPlaceholder(id.split("_")[0] + id.split("_")[1]).build();
        return channelSelectMenu;
    }

    public SelectMenu getRoleSelectMenu(Predicate<Role> pred, String id) {
        System.out.println(id);
        LinkedList<Role> roles = new LinkedList<>(guild.getRoles());
        roles.removeIf(pred);
        LinkedList<SelectOption> roleOptions = new LinkedList<>();

        for (Role role : roles) {
            SelectOption opt = SelectOption.of(role.getName(), role.getId());
            roleOptions.add(opt);
        }

        SelectMenu roleSelectMenu = SelectMenu.create(guild.getName() + "_" + id).addOptions(roleOptions)
                .setPlaceholder(id.split("_")[0] + " " + id.split("_")[1]).build();
        return roleSelectMenu;
    }

    public SelectMenu getManagerSelectMenu(Team t) {
        LinkedList<Role> roles = new LinkedList<>(guild.getRoles());
        roles.removeIf((Role r) -> !r.getName().toLowerCase().contains("manager")
                && !r.getName().toLowerCase().contains("admin") && !r.getName().toLowerCase().contains("staff"));
        List<Member> members = DiscordBot.getInstance().getMemberOfRole(guild, roles.toArray(new Role[0]));
        LinkedList<SelectOption> memberOptions = new LinkedList<>();
        for (Member member : members) {
            SelectOption opt = SelectOption.of(member.getUser().getName(), member.getUser().getId());
            memberOptions.add(opt);
        }
        SelectMenu memberSelectMenu = SelectMenu.create(t.getName() + "_managerSelect").addOptions(memberOptions)
                .setPlaceholder(t.getName() + "manager Select").build();
        return memberSelectMenu;
    }

    public SelectMenu getCoachSelectMenu(Team t) {
        LinkedList<Role> roles = new LinkedList<>(guild.getRoles());
        roles.removeIf((Role r) -> !r.getName().toLowerCase().contains("Coach")
                && !r.getName().toLowerCase().contains("coach"));
        List<Member> members = DiscordBot.getInstance().getMemberOfRole(guild, roles.toArray(new Role[0]));
        LinkedList<SelectOption> memberOptions = new LinkedList<>();
        for (Member member : members) {
            if (memberOptions.size() < 25) {
                SelectOption opt = SelectOption.of(member.getUser().getName(), member.getUser().getId());
                memberOptions.add(opt);
            }
        }
        SelectMenu memberSelectMenu = SelectMenu.create(t.getName() + "_coachSelect").addOptions(memberOptions)
                .setPlaceholder(t.getName() + "coach Select").build();
        return memberSelectMenu;
    }

    public MessageCreateData createConfig() {

        // clear channel
        List<Message> messages = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete()
                .getRetrievedHistory();
        // send messages
        MessageCreateBuilder message = new MessageCreateBuilder();
        message.setContent(
                "This is the configuration data for this server and its teams. The first message contains the details of this server's attributes. The bot will use these attributes to do actions such as sending a sub request to the representive channel. When editing these values by clicking the respective button below the embed, the bot will find channels and roles with similar key words. For example, when changing the sub channel, the bot will search for other channels whos name contains 'sub'.");
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(guild.getName() + " config");

        Field subChannelField = new Field("Sub Channel", subChannel.getAsMention(), true);
        Field subRoleField = new Field("Sub Role", subRole.getAsMention(), true);
        Field configChannelField = new Field("Config Channel", botConfigChannel.getAsMention(), true);
        Field differentTeamSheetsField = new Field("has Different sheets for teams",
                differentTeamSheetSetups ? "true" : "false", true);
        Field sheetConfigField = new Field("sheet config", sheetConfig.toJSON(), false);

        embed.addField(subChannelField);
        embed.addField(subRoleField);
        embed.addBlankField(false);
        embed.addField(configChannelField);
        embed.addField(differentTeamSheetsField);
        embed.addBlankField(false);
        embed.addField(sheetConfigField);
        message.addEmbeds(embed.build());

        Button editSubChannel = Button.danger(String.format("%s_%s", guild.getName(), "editSubChannel"),
                "change sub channel");
        Button editSubRole = Button.danger(String.format("%s_%s", guild.getName(), "editSubRole"), "change sub role");
        // Button editConfigChannel = Button.primary(String.format("%s_%s",
        // guild.getName(), "editCoinfigChannel"),
        // "edit config channel");
        Button toggleDifferentSheets = Button.success(String.format("%s_%s", guild.getName(), "toggleDifferentSheets"),
                "toggle different sheets");
        Button editConfigJson = Button.primary(String.format("%s_%s", guild.getName(), "editConfigJSON"),
                "edit the sheet config");

        message.addActionRow(editSubChannel, editSubRole);
        message.addActionRow(toggleDifferentSheets);
        message.addActionRow(editConfigJson);

        return message.build();
    }

    public boolean hasTeam(String name) {
        return teams.containsKey(name);
    }

    public void addTeam(Team t) {
        teams.put(t.getName(), t);

        if (teams.size() > 1) {
            guild.updateCommands().addCommands(
                    Commands.slash("makeconfigchannel",
                            "sets the current channel for the guild to the bot config channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("updateevents", "re-freshes an event details")
                            .addOption(OptionType.ROLE, "team", "Main roster role for the team")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("role", "edit role of a player")
                            .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                            .addOption(OptionType.ROLE, "newplayerrole", "role to make the player")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("sort", "sort the events of a channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("removesubs", "removes the subs of a given team")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("help", "request the bot's manual")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "edit responses")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "edit details")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "delete event")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("newteam", "create a new team")
                            .addOption(OptionType.STRING, "name", "name of the team", true, false)
                            .addOption(OptionType.STRING, "abbv", "abbreviation of the name, for sheet config",
                                    true, false)
                            .addOption(OptionType.STRING, "minrank", "min rank for subs", true, false)
                            .addOption(OptionType.CHANNEL, "timetable", "timetable / schedule channel",
                                    true, false)
                            .addOption(OptionType.CHANNEL, "announcement", "announcement channel", true,
                                    false)
                            .addOption(OptionType.ROLE, "roster", "roster role", true, false)
                            .addOption(OptionType.ROLE, "trial", "trial role", true, false)
                            .addOption(OptionType.ROLE, "sub", "sub role", true, false)
                            .addOption(OptionType.USER, "manager", "team manager", true, false)
                            .addOption(OptionType.USER, "coach", "team coach", true, false)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("rosters",
                            "create a set of messages containing the list of teams and players in your server")
                            .addOption(OptionType.CHANNEL, "rosterchannel", "channel to send team list to", true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))

                    .queue((res) -> {
                        System.out.println("updated commands");
                    });
        } else if (teams.size() == 1) {
            guild.updateCommands().addCommands(
                    Commands.slash("makeconfigchannel",
                            "sets the current channel for the guild to the bot config channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("updateevents", "re-freshes an event details")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("role", "edit role of a player")
                            .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                            .addOption(OptionType.ROLE, "newplayerrole", "role to make the player")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("sort", "sort the events of a channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("removesubs", "removes the subs of a given team")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("help", "request the bot's manual")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "edit responses")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "edit details")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.context(Type.MESSAGE, "delete event")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("newteam", "create a new team")
                            .addOption(OptionType.STRING, "name", "name of the team", true, false)
                            .addOption(OptionType.STRING, "abbv", "abbreviation of the name, for sheet config",
                                    true, false)
                            .addOption(OptionType.STRING, "minrank", "min rank for subs", true, false)
                            .addOption(OptionType.CHANNEL, "timetable", "timetable / schedule channel",
                                    true, false)
                            .addOption(OptionType.CHANNEL, "announcement", "announcement channel", true,
                                    false)
                            .addOption(OptionType.ROLE, "roster", "roster role", true, false)
                            .addOption(OptionType.ROLE, "trial", "trial role", true, false)
                            .addOption(OptionType.ROLE, "sub", "sub role", true, false)
                            .addOption(OptionType.USER, "manager", "team manager", true, false)
                            .addOption(OptionType.USER, "coach", "team coach", true, false)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("rosters",
                            "create a set of messages containing the list of teams and players in your server")
                            .addOption(OptionType.CHANNEL, "rosterchannel", "channel to send team list to", true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                    .queue();
            this.soloTeam = t;

        }

    }

    public static Server getGuild(Long id) {
        return repoos.get(id);
    }

    public net.dv8tion.jda.api.entities.Guild getGuild() {
        return guild;
    }

    public void setGuild(net.dv8tion.jda.api.entities.Guild guild) {
        this.guild = guild;
    }

    public MessageChannel getSubChannel() {
        return subChannel;
    }

    public void setSubChannel(MessageChannel subChannel) {
        this.subChannel = subChannel;
    }

    public HashMap<String, Team> getTeams() {
        return teams;
    }

    public List<Team> getTeamsAsList() {
        return List.copyOf(teams.values());
    }

    public void setTeams(HashMap<String, Team> teams) {
        this.teams = teams;
    }

    public Role getSubRole() {
        return subRole;
    }

    public void setSubRole(Role subRole) {
        this.subRole = subRole;
    }

    public MessageChannel getBotConfigChannel() {
        return botConfigChannel;
    }

    public void setBotConfigChannel(MessageChannel botConfigChannel) {
        this.botConfigChannel = botConfigChannel;
    }

    public static HashMap<Long, Server> getRepoos() {
        return repoos;
    }

    public static void setRepoos(HashMap<Long, Server> repoos) {
        Server.repoos = repoos;
    }

    public boolean isDifferentTeamSheetSetups() {
        return differentTeamSheetSetups;
    }

    public void setDifferentTeamSheetSetups(boolean differentTeamSheetSetups) {
        differentTeamSheetSetups = differentTeamSheetSetups;
    }

    public GoogleSheet getSheet() {
        return sheet;
    }

    public void setSheet(GoogleSheet sheet) {
        this.sheet = sheet;
    }

    public SheetConfig getSheetConfig() {
        return sheetConfig;
    }

    public void setSheetConfig(SheetConfig sheetConfig) {
        this.sheetConfig = sheetConfig;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }
}
