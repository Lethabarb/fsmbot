package FSM.entities;

import java.io.File;
import java.lang.StackWalker.Option;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import okhttp3.internal.connection.RouteSelector.Selection;

public class Server extends ListenerAdapter implements Runnable {
    private static HashMap<Long, Server> repoos = new HashMap<>();
    private HashMap<String, Team> teams = new HashMap<>();
    private net.dv8tion.jda.api.entities.Guild guild;
    private MessageChannel subChannel;
    private Role subRole = null;
    private MessageChannel botConfigChannel;
    private boolean differentTeamSheetSetups = false;
    private GoogleSheet sheet;
    private SheetConfig sheetConfig;
    private Thread t;
    private Message configEditMessage = null;
    private MessageCreateBuilder configEditBuilder = null;

    // public Server(Guild guild) {
    // List<GuildChannel> channels = guild.getChannels();
    // for (GuildChannel guildChannel : channels) {
    // if (guildChannel.getName().equalsIgnoreCase("fsm-config")) {
    // botConfigChannel = guild.getTextChannelById(guildChannel.getId());
    // }
    // }

    // }

    public Server(Guild guild, MessageChannel subChannel, Role subRole, SheetConfig sheetConfig, Team... teams) {
        System.out.println("Creating " + guild.getName() + " Server");
        this.guild = guild;
        this.subChannel = subChannel;
        this.subRole = subRole;
        this.sheetConfig = sheetConfig;
        System.out.println("adding commands...");

        System.out.println("adding config command");
        guild.updateCommands().addCommands(
                Commands.slash("makeconfigchannel",
                        "sets the current channel for the guild to the bot config channel"),
                Commands.slash("update", "re-freshes an event details")
                        .addSubcommands(new SubcommandData("events", "updates all events in all servers")
                                .addOption(OptionType.ROLE, "teamrole", "role of the team to update events for")),
                Commands.slash("role", "edit role of a player")
                        .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                        .addOption(OptionType.ROLE, "newplayerrole", "role to make the player"),
                Commands.slash("sort", "sort the events of a channel"),
                Commands.slash("removesubs", "removes the subs of a given team"),
                Commands.slash("help", "request the bot's manual"),
                Commands.context(Type.MESSAGE, "edit responses"),
                Commands.context(Type.MESSAGE, "edit details"),
                Commands.context(Type.MESSAGE, "delete event"))
                .queue();

        System.out.println("added commands");
        // List<GuildChannel> channels = guild.getChannels();
        // for (GuildChannel guildChannel : channels) {
        // if (guildChannel.getName().equalsIgnoreCase("fsm-config"));
        // }
        for (Team t : teams) {
            this.teams.put(t.getName(), t);
        }
        repoos.put(guild.getIdLong(), this);
        this.t = new Thread(this, guild.getName());
        this.t.start();
    }

    @Override
    public void run() {
        DiscordBot bot = DiscordBot.getInstance();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        while (true) {
            // ArrayList<Team> teamsList = new ArrayList<>(teams.values());
            for (Team team : teams.values()) {
                bot.updateScrims(team);
                while (bot.getQueue() > 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                bot.sortChannel(team.getTimetable());
            }
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
            if (botConfigChannel != null && slashCommand.getChannel().getId().equalsIgnoreCase(botConfigChannel.getId())) {
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
                List<Message> messageHist = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete().getRetrievedHistory();
                if (messageHist.size() > 0) botConfigChannel.purgeMessages(messageHist);
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

        } else if (command.equalsIgnoreCase("update")) {
            InteractionHook reply = slashCommand.deferReply(true).complete();
            try {
                reply.editOriginal("finding team").queue();
                Role rosterRole = slashCommand.getOption("teamrole").getAsRole();
                for (Team t : teams.values()) {
                    if (t.getRosterRole().getId().equalsIgnoreCase(rosterRole.getId())) {
                        reply.editOriginal("found, updating").queue();
                        DiscordBot.getInstance().updateAllEvents(t);
                        reply.editOriginal("finished").queue();
                        reply.deleteOriginal().queue();
                    }
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
        }
    }

    public synchronized void createEditingMessage() {
        Button submitEditButton = Button.primary(String.format("%s_submitConfigEdit", guild.getName()), "submit");
        if (configEditMessage == null && configEditBuilder == null) {
            configEditBuilder = new MessageCreateBuilder().setContent("waiting for interactions.");
            configEditBuilder.addActionRow(submitEditButton);
            DiscordBot.addQueue();

            botConfigChannel.sendMessage(configEditBuilder.build()).queue((res) -> {
                configEditMessage = res;
                DiscordBot.subtractQueue();
            });
        } else {
            configEditBuilder.setContent("");
        }
        while (DiscordBot.getQueue() > 0) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // TODO: handle exception
            }
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
        String buttonUse = data[1];
        if (buttonUse.equalsIgnoreCase("editSubChannel")) {
            // SelectMenu channels = SelectMenu.create("channelsselectmenu")
            // guild.getTextChannels()
            createEditingMessage();

            SelectMenu channelSelectMenu = getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("sub"), "subChannelEdit");
            EmbedBuilder embed = new EmbedBuilder();
            configEditBuilder.addActionRow(channelSelectMenu);
            configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            buttonEvent.reply("added field").setEphemeral(true).queue();
        } else if (buttonUse.equalsIgnoreCase("editSubRole")) {
            createEditingMessage();
            SelectMenu channelSelectMenu = getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("sub"),
                    "subRoleEdit");

            configEditBuilder.addActionRow(channelSelectMenu);
            configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            buttonEvent.reply("added field").setEphemeral(true).queue();
        } else if (buttonUse.equalsIgnoreCase("editCoinfigChannel")) {
            createEditingMessage();
            SelectMenu channelSelectMenu = getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("config"), "configChannelEdit");

            configEditBuilder.addActionRow(channelSelectMenu);
            configEditMessage.editMessage(MessageEditData.fromCreateData(configEditBuilder.build())).queue();
            buttonEvent.reply("added field").setEphemeral(true).queue();
        } else if (buttonUse.equalsIgnoreCase("toggleDifferentSheets")) {
            differentTeamSheetSetups = !differentTeamSheetSetups;
        } else if (buttonUse.equalsIgnoreCase("editConfigJSON")) {
            createEditingMessage();
            TextInput textfield = TextInput.create("configJsonTextInput", "config JSON", TextInputStyle.PARAGRAPH)
                    .build();
            buttonEvent
                    .replyModal(
                            Modal.create("editConfigJSON", "edit sheet config JSON").addActionRow(textfield).build())
                    .queue();
        } else if (buttonUse.equalsIgnoreCase("submitConfigEdit")) {
            configEditBuilder = null;
            configEditMessage.delete().queue();
            configEditMessage = null;
        }
    }

    public void updateConfigMessage() {
        List<Message> messageHistory = MessageHistory.getHistoryFromBeginning(botConfigChannel).complete().getRetrievedHistory();
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

    @Override
    public void onSelectMenuInteraction(@Nonnull SelectMenuInteractionEvent event) {
        InteractionHook reply = event.deferReply(true).complete();
        reply.editOriginal("editing").queue();
        int len = event.getSelectMenu().getId().split("_").length;
        String use = event.getSelectMenu().getId();
        String teamName = "";
        if (len > 1) {
            use = event.getSelectMenu().getId().split("_")[0];
            teamName = event.getSelectMenu().getId().split("_")[1];
        }
        Team team = teams.get(teamName);
        String value = event.getValues().get(0);
        if (use.equalsIgnoreCase("subChannelEdit")) {
            subChannel = guild.getTextChannelById(value);
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("subRoleEdit")) {
            subRole = guild.getRoleById(value);
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("configChannelEdit")) {
            botConfigChannel = guild.getTextChannelById(value);
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("managerSelect")) {
            team.setManager(guild.getMemberById(value).getUser());
            team.updateConfigMessage();
        } else if (use.equalsIgnoreCase("timetableEdit")) {
            team.setTimetable(guild.getTextChannelById(value));
            team.updateConfigMessage();
        } else if (use.equalsIgnoreCase("announceEdit")) {
            team.setAnnouncement(guild.getTextChannelById(value));
            team.updateConfigMessage();
        } else if (use.equalsIgnoreCase("rosterEdit")) {
            team.setRosterRole(guild.getRoleById(value));
            team.updateConfigMessage();
        } else if (use.equalsIgnoreCase("trialEdit")) {
            team.setTrialRole(guild.getRoleById(value));
            team.updateConfigMessage();
        } else if (use.equalsIgnoreCase("subEdit")) {
            team.setSubRole(guild.getRoleById(value));
            team.updateConfigMessage();
        }
        reply.deleteOriginal().queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent modalEvent) {
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

        SelectMenu channelSelectMenu = SelectMenu.create(id).addOptions(channelOptions).setPlaceholder(id).build();
        return channelSelectMenu;
    }

    public SelectMenu getRoleSelectMenu(Predicate<Role> pred, String id) {
        LinkedList<Role> roles = new LinkedList<>(guild.getRoles());
        roles.removeIf(pred);
        LinkedList<SelectOption> roleOptions = new LinkedList<>();

        for (Role role : roles) {
            SelectOption opt = SelectOption.of(role.getName(), role.getId());
            roleOptions.add(opt);
        }

        SelectMenu roleSelectMenu = SelectMenu.create(id).addOptions(roleOptions).setPlaceholder(id).build();
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
        SelectMenu memberSelectMenu = SelectMenu.create("managerSelect_" + t.getName()).addOptions(memberOptions)
                .setPlaceholder("managerSelect").build();
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
        Button editConfigChannel = Button.primary(String.format("%s_%s", guild.getName(), "editCoinfigChannel"),
                "edit config channel");
        Button toggleDifferentSheets = Button.success(String.format("%s_%s", guild.getName(), "toggleDifferentSheets"),
                "toggle different sheets");
        Button editConfigJson = Button.primary(String.format("%s_%s", guild.getName(), "editConfigJSON"),
                "edit the sheet config");

        message.addActionRow(editSubChannel, editSubRole);
        message.addActionRow(toggleDifferentSheets);
        message.addActionRow(editConfigChannel, editConfigJson);

        return message.build();
    }

    public void addTeam(Team t) {
        teams.put(t.getName(), t);
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

    public Message getConfigEditMessage() {
        return configEditMessage;
    }

    public void setConfigEditMessage(Message configEditMessage) {
        this.configEditMessage = configEditMessage;
    }

    public MessageCreateBuilder getConfigEditBuilder() {
        return configEditBuilder;
    }

    public void setConfigEditBuilder(MessageCreateBuilder configEditBuilder) {
        this.configEditBuilder = configEditBuilder;
    }

    // public Role getDpsRole() {
    // return dpsRole;
    // }

    // public void setDpsRole(Role dpsRole) {
    // this.dpsRole = dpsRole;
    // }

    // public Role getTankRole() {
    // return tankRole;
    // }

    // public void setTankRole(Role tankRole) {
    // this.tankRole = tankRole;
    // }

    // public Role getSuppRole() {
    // return suppRole;
    // }

    // public void setSuppRole(Role suppRole) {
    // this.suppRole = suppRole;
    // }
}
