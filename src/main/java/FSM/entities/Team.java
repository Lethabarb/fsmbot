package FSM.entities;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.google.gson.Gson;

import FSM.services.DiscordBot;
import FSM.services.GoogleSheet;
import FSM.services.GoogleSheet2;
import io.opencensus.trace.Link;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class Team extends ListenerAdapter {
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
    private User manager;
    private LinkedList<Event> events = new LinkedList<>();

    private GoogleSheet2 sheet;
    private SheetConfig sheetConfig;

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

    public SheetConfig getSheetConfig() {
        return sheetConfig;
    }

    public void setSheetConfig(SheetConfig sheetConfig) {
        this.sheetConfig = sheetConfig;
    }

    public Team(String name, String nameAbbv, String minRank, MessageChannel timetable, MessageChannel announcement,
            Role rosterRole, Role trialRole,
            Role subRole, List<Member> members, int teamupSubCalendar, String sheetId, User manager) {
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
        this.sheet = new GoogleSheet2();
        this.manager = manager;
        // Thread t = new Thread(this, name);
        teams.add(this);
        // t.start();
    }

    public Team(String name, String nameAbbv, String minRank, MessageChannel timetable, MessageChannel announcement,
            Role rosterRole, Role trialRole,
            Role subRole, User manager) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetable = timetable;
        this.announcement = announcement;
        this.rosterRole = rosterRole;
        this.trialRole = trialRole;
        this.subRole = subRole;
        this.sheet = new GoogleSheet2();
        this.manager = manager;
        // Thread t = new Thread(this, name);
        teams.add(this);
        // t.start();
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
        this.sheet = new GoogleSheet2();
        // Thread t = new Thread(this, name);
        teams.add(this);
        // t.start();
    }

    // @Override
    // public void run() {
    // DiscordBot bot = DiscordBot.getInstance();
    // Boolean first = true;
    // while (true) {
    // int c = 0;
    // while (!avail) {
    // try {
    // Thread.sleep(1000);
    // System.out.println(name + " is waiting to update..." + c++);
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    // }
    // try {
    // System.out.println("==========" + name + "==========");
    // avail = false;
    // try {
    // if (first) {
    // bot.createEventsFromChanel(timetable, this);
    // }
    // first = false;
    // bot.updateScrims(this);
    // bot.sortChannel(timetable);
    // } catch (Exception e) {
    // e.printStackTrace();
    // // TODO: handle exception
    // } finally {
    // avail = true;
    // }
    // Thread.sleep(12 * 60 * 60 * 1000);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // }

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

    public GoogleSheet2 getSheet() {
        return sheet;
    }

    public void setSheet(GoogleSheet2 sheet) {
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
            if (team.getName().equalsIgnoreCase(name))
                return team;
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

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public boolean hasRosterOrTrialRole(Member member) {
        String rosterRoleId = rosterRole.getId();
        String trialRoleId = trialRole.getId();
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

    public MessageCreateData createConfig() {
        MessageCreateBuilder message = new MessageCreateBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s (%s) Config", name, nameAbbv));

        Field minRankField = new Field("min rank", minRank, true);
        Field timetableField = new Field("timetable", timetable.getAsMention(), true);
        Field announcementField = new Field("announcement", announcement.getAsMention(), true);
        Field rosterRoleField = new Field("roster role", rosterRole.getAsMention(), true);
        Field trialRoleField = new Field("trial role", trialRole.getAsMention(), true);
        Field subRoleField = new Field("sub role", subRole.getAsMention(), true);
        Field teamupField = new Field("teamup id", String.valueOf(teamupSubCalendar), true);
        Field managerField = new Field("manager", manager.getAsMention(), true);

        embed.addField(minRankField);
        embed.addField(teamupField);
        embed.addField(managerField);
        embed.addBlankField(false);
        embed.addField(timetableField);
        embed.addField(announcementField);
        embed.addBlankField(false);
        embed.addField(rosterRoleField);
        embed.addField(trialRoleField);
        embed.addField(subRoleField);

        if (guild.isDifferentTeamSheetSetups()) {
            Field sheetConfigField = new Field("Sheet Config", sheetConfig.toJSON(), false);
            embed.addField(sheetConfigField);
        }

        message.addEmbeds(embed.build());

        Button editName = Button.success(String.format("%s_%s", hashCode(), "editName"), "edit name");
        Button editAbbv = Button.success(String.format("%s_%s", hashCode(), "editAbbv"), "edit name abbreviation");
        Button editMinRank = Button.danger(String.format("%s_%s", hashCode(), "editMinRank"), "edit min rank");
        Button editTeamUp = Button.danger(String.format("%s_%s", hashCode(), "editTeamUp"), "edit team up ID");
        Button editManager = Button.danger(String.format("%s_%s", hashCode(), "editManager"), "change manager");
        Button editTimetable = Button.primary(String.format("%s_%s", hashCode(), "editTimeTable"),
                "change timetable channel");
        Button editAnnounce = Button.primary(String.format("%s_%s", hashCode(), "editAnnouncement"),
                "change announcement channel");
        Button editRoster = Button.secondary(String.format("%s_%s", hashCode(), "editRosterRole"),
                "change roster role");
        Button editTrial = Button.secondary(String.format("%s_%s", hashCode(), "editTrialRole"), "change trial role");
        Button editSub = Button.secondary(String.format("%s_%s", hashCode(), "editSubRole"), "change sub role");

        message.addActionRow(editName, editAbbv);
        message.addActionRow(editMinRank, editTeamUp, editManager);
        message.addActionRow(editTimetable, editAnnounce);
        message.addActionRow(editRoster, editTrial, editSub);

        if (guild.isDifferentTeamSheetSetups()) {
            Button editSheetConfig = Button.success(hashCode() + "_editConfig", "edit sheet config");
            message.addActionRow(editSheetConfig);
        }

        return message.build();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
        String[] data = buttonEvent.getButton().getId().split("_");
        if (!data[0].equalsIgnoreCase(String.valueOf(hashCode())))
            return;

        String buttonUse = data[1];
        if (buttonUse.equalsIgnoreCase("editMinRank")) {

            // TextInput textfield = TextInput.create("textInput", "rank", TextInputStyle.SHORT).setValue(minRank).build();
            // buttonEvent
            //         .replyModal(
            //                 Modal.create(name + "_editMinRank", "edit team's min rank").addActionRow(textfield).build())
            //         .queue();

            LinkedList<SelectOption> options = new LinkedList<>();
            String[] ranks = {"Bronze", "Silver","Gold","Platnium","Diamond","Master","Grand-Master"};
            String[] values = {"5", "3", "1"};
            for (String string : ranks) {
                    String value1 = string + values[0];
                    String value2 = string + values[1];
                    String value3 = string + values[2];
                    SelectOption opt1 = SelectOption.of(value1, value1);
                    SelectOption opt2 = SelectOption.of(value2, value2);
                    SelectOption opt3 = SelectOption.of(value3, value3);
                    options.add(opt1);
                    options.add(opt2);
                    options.add(opt3);
            }
            options.add(SelectOption.of("T500", "T500"));
            SelectMenu selectMenu =  SelectMenu.create(name + "_editMinRank").addOptions(options).build();
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editName")) {

            TextInput textfield = TextInput.create("textInput", "Name", TextInputStyle.SHORT).setValue(name).build();
            buttonEvent
                    .replyModal(Modal.create(name + "_editName", "edit team's name")
                            .addActionRow(textfield).build())
                    .queue();
        } else if (buttonUse.equalsIgnoreCase("editAbbv")) {

            TextInput textfield = TextInput.create("textInput", "Name Abbv", TextInputStyle.SHORT).setValue(nameAbbv)
                    .build();
            buttonEvent
                    .replyModal(Modal.create(name + "_editAbbv", "Edit Team Name Abbreviation")
                            .addActionRow(textfield).build())
                    .queue();
        } else if (buttonUse.equalsIgnoreCase("editTeamUp")) {

            TextInput textfield = TextInput.create("textInput", "team up sub-id", TextInputStyle.SHORT).build();
            buttonEvent
                    .replyModal(Modal.create(name + "_editTeamUp", "edit team's team up sub calendar ID")
                            .addActionRow(textfield).build())
                    .queue();

        } else if (buttonUse.equalsIgnoreCase("editManager")) {
            SelectMenu selectMenu = guild.getManagerSelectMenu(this);
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editTimeTable")) {
            SelectMenu selectMenu = guild
                    .getChannelSelectMenu((TextChannel c) -> !c.getName().toLowerCase().contains("schedule")
                            && !c.getName().toLowerCase().contains("timetable"), name + "_timetableEdit");
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editAnnouncement")) {
            SelectMenu selectMenu = guild.getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("announce"), name + "_announceEdit");
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editRosterRole")) {
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("")
                    && !r.getName().toLowerCase().contains("main"), name + "_rosterEdit");
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editTrialRole")) {
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("trial"),
                    name + "_trialEdit");
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editSubRole")) {
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("sub"),
                    name + "_subEdit");
            guild.addEditingComponent(ActionRow.of(selectMenu), buttonEvent);

        } else if (buttonUse.equalsIgnoreCase("editConfig")) {
            TextInput textfield = TextInput.create("textInput", "config JSON", TextInputStyle.PARAGRAPH)
                    .setValue(sheetConfig.toJSON()).build();
            buttonEvent
                    .replyModal(
                            Modal.create(name + "_editConfigJSON", "edit sheet config JSON").addActionRow(textfield)
                                    .build())
                    .queue();
        }
    }

    public void updateConfigMessage() {
        List<Message> messageHistory = MessageHistory.getHistoryFromBeginning(guild.getBotConfigChannel()).complete()
                .getRetrievedHistory();
        for (Message message : messageHistory) {
            try {
                if (message.getEmbeds().get(0).getTitle()
                        .equalsIgnoreCase(String.format("%s (%s) Config", name, nameAbbv))) {
                    message.editMessage(MessageEditData.fromCreateData(createConfig())).queue();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void updateConfigMessage(String oldNmae) {
        List<Message> messageHistory = MessageHistory.getHistoryFromBeginning(guild.getBotConfigChannel()).complete()
                .getRetrievedHistory();
        for (Message message : messageHistory) {
            try {
                if (message.getEmbeds().get(0).getTitle()
                        .equalsIgnoreCase(String.format("%s (%s) Config", oldNmae, nameAbbv))) {
                    message.editMessage(MessageEditData.fromCreateData(createConfig())).queue();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void updateConfigMessage(String oldAbbv, boolean abbv) {
        List<Message> messageHistory = MessageHistory.getHistoryFromBeginning(guild.getBotConfigChannel()).complete()
                .getRetrievedHistory();
        for (Message message : messageHistory) {
            try {
                if (message.getEmbeds().get(0).getTitle()
                        .equalsIgnoreCase(String.format("%s (%s) Config", name, oldAbbv))) {
                    message.editMessage(MessageEditData.fromCreateData(createConfig())).queue();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void sendRosterMessage(MessageChannel c) {
        MessageCreateBuilder message = new MessageCreateBuilder();
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(name);
        embed.setDescription(rosterRole.getAsMention());
        embed.setColor(rosterRole.getColor());

        String tanksString = "";
        String dpsString = "";
        String supportString = "";

        for (Member m : members) {
            Player p = Player.getPlayer(m);
            if (p.getRole() == Player.DPS) dpsString += m.getAsMention() + "\n";
            if (p.getRole() == Player.TANK) tanksString += m.getAsMention() + "\n";
            if (p.getRole() == Player.SUPPORT) supportString += m.getAsMention() + "\n";
        }

        Field tankField = new Field("Tanks", tanksString, true);
        Field dpsField = new Field("DPS", dpsString, true);
        Field supportField = new Field("Support", supportString, true);

        embed.addField(tankField);
        embed.addField(dpsField);
        embed.addField(supportField);

        message.addEmbeds(embed.build());

        c.sendMessage(message.build()).queue();

    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        System.out.println(event.getModalId().split("_")[0] + " == " + name);
        if (!event.getModalId().split("_")[0].equalsIgnoreCase(name))
            return;
        String reason = event.getModalId().split("_")[1];
        ModalMapping textInput = event.getValue("textInput");
        ModalMapping memberId = event.getValue("memberSelectMenu");
        ModalMapping roleId = event.getValue("roleSelectMenu");
        ModalMapping channelId = event.getValue("trialSelectMenu");
        String oldName = name;
        String oldAbbv = nameAbbv;
        try {
            if (textInput != null && reason.equalsIgnoreCase("editMinRank"))
                minRank = textInput.getAsString();
            else if (textInput != null && reason.equalsIgnoreCase("editTeamUp"))
                teamupSubCalendar = Integer.parseInt(textInput.getAsString());
            else if (memberId != null)
                manager = guild.getGuild().getMemberById(memberId.getAsString()).getUser();
            else if (channelId != null && event.getModalId().equalsIgnoreCase("editTimeTable"))
                timetable = guild.getGuild().getTextChannelById(channelId.getAsString());
            else if (channelId != null && event.getModalId().equalsIgnoreCase("editAnnouncement"))
                announcement = guild.getGuild().getTextChannelById(channelId.getAsString());
            else if (roleId != null && event.getModalId().equalsIgnoreCase("editRosterRole"))
                rosterRole = guild.getGuild().getRoleById(roleId.getAsString());
            else if (roleId != null && event.getModalId().equalsIgnoreCase("editTrialRole"))
                trialRole = guild.getGuild().getRoleById(roleId.getAsString());
            else if (roleId != null && event.getModalId().equalsIgnoreCase("editSubRole"))
                subRole = guild.getGuild().getRoleById(roleId.getAsString());
            else if (textInput != null && reason.equalsIgnoreCase("editName"))
                name = textInput.getAsString();
            else if (textInput != null && reason.equalsIgnoreCase("editAbbv"))
                nameAbbv = textInput.getAsString();
            else if (textInput != null && reason.equalsIgnoreCase("editConfigJSON"))
                sheetConfig = new Gson().fromJson(textInput.getAsString(), sheetConfig.getClass());
        } catch (Exception e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
        if (reason.equalsIgnoreCase("editName")) {
            updateConfigMessage(oldName);
        } else if (reason.equalsIgnoreCase("editAbbv")) {
            updateConfigMessage(oldAbbv, false);
        } else {
            updateConfigMessage();
        }
        // guild.createConfig();
        event.reply("updated").setEphemeral(true).queue();
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        String[] eventData = event.getSelectMenu().getId().split("_");
        if (!eventData[0].equalsIgnoreCase(name))
            return;
        InteractionHook reply = event.deferReply(true).complete();
        reply.editOriginal("editing").queue();
        String use = eventData[1];
            String value = event.getValues().get(0);
        if (use.equalsIgnoreCase("managerSelect")) {
            setManager(guild.getGuild().getMemberById(value).getUser());
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("timetableEdit")) {
            setTimetable(guild.getGuild().getTextChannelById(value));
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("announceEdit")) {
            setAnnouncement(guild.getGuild().getTextChannelById(value));
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("rosterEdit")) {
            setRosterRole(guild.getGuild().getRoleById(value));
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("trialEdit")) {
            setTrialRole(guild.getGuild().getRoleById(value));
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("subEdit")) {
            setSubRole(guild.getGuild().getRoleById(value));
            updateConfigMessage();
        } else if (use.equalsIgnoreCase("editMinRank")) {
            setMinRank(value);
            updateConfigMessage();
        }
        reply.deleteOriginal().queue();
        guild.removeActionRow(event.getSelectMenu().getId());
        guild.createEditingMessage();
    }

    public LinkedList<Event> getEvents() {
        return events;
    }

    public LinkedList<Event> getTodaysEvents() {
        ZonedDateTime today = ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
        LinkedList<Event> res = new LinkedList<>();
        for (Event e : events) {
            if (e.getDateTime().getDayOfMonth() == today.getDayOfMonth() && e.getDateTime().getMonthValue() == today.getMonthValue() && e.getDateTime().getYear() == today.getYear()) {
                res.add(e);
            }
        }
        return res;
    }

    public void setEvents(LinkedList<Event> events) {
        this.events = events;
    }

    public void addEvent(Event e) {
        events.add(e);
    }

    public synchronized void checkEventSubRequests() {
        System.out.println(String.format("[%s]: checking sub requests", name));
        for (Event event : events) {
            event.checkSubRequests();
        }
        System.out.println(String.format("[%s]: finished checking sub requests", name));
    }

}
