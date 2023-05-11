package FSM.entities;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

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
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
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

        message.addEmbeds(embed.build());

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

        message.addActionRow(editMinRank, editTeamUp, editManager);
        message.addActionRow(editTimetable, editAnnounce);
        message.addActionRow(editRoster, editTrial, editSub);

        return message.build();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
        String[] data = buttonEvent.getButton().getId().split("_");
        if (!data[0].equalsIgnoreCase(String.valueOf(hashCode())))
            return;
        Button submitEditButton = Button.primary(String.format("%s_submitConfigEdit", guild.getGuild().getName()),
                "submit");

        String buttonUse = data[1];
        if (buttonUse.equalsIgnoreCase("editMinRank")) {

            TextInput textfield = TextInput.create("textInput", "rank", TextInputStyle.SHORT).build();
            buttonEvent.replyModal(Modal.create("editMinRank", "edit team's min rank").addActionRow(textfield).build())
                    .queue();

        } else if (buttonUse.equalsIgnoreCase("editTeamUp")) {

            TextInput textfield = TextInput.create("textInput", "team up sub-id", TextInputStyle.SHORT).build();
            buttonEvent
                    .replyModal(Modal.create("editTeamUp", "edit team's team up sub calendar ID")
                            .addActionRow(textfield).build())
                    .queue();

        } else if (buttonUse.equalsIgnoreCase("editManager")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild.getManagerSelectMenu(this);
            // guild.getConfigEditBuilder().addActionRow(selectMenu);
            // guild.getConfigEditMessage()
            //         .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();

            MessageCreateBuilder messageData = guild.getConfigEditBuilder();
            messageData.addActionRow(selectMenu);
            guild.setConfigEditBuilder(messageData);

            Message message = guild.getConfigEditMessage();
            message.editMessage(MessageEditData.fromCreateData(messageData.build())).queue((res) -> {
                guild.setConfigEditMessage(res);
            });
        } else if (buttonUse.equalsIgnoreCase("editTimeTable")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild
                    .getChannelSelectMenu((TextChannel c) -> !c.getName().toLowerCase().contains("schedule")
                            && !c.getName().toLowerCase().contains("timetable"), "timetableEdit_" + name);
            guild.getConfigEditBuilder().addActionRow(selectMenu);
            guild.getConfigEditMessage()
                    .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();;
                        InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("added field").queue();
            reply.deleteOriginal().queue();
        } else if (buttonUse.equalsIgnoreCase("editAnnouncement")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild.getChannelSelectMenu(
                    (TextChannel c) -> !c.getName().toLowerCase().contains("announce"), "announceEdit_" + name);
            guild.getConfigEditBuilder().addActionRow(selectMenu);
            guild.getConfigEditMessage()
                    .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();;
                        InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("added field").queue();
            reply.deleteOriginal().queue();
        } else if (buttonUse.equalsIgnoreCase("editRosterRole")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("roster")
                    && !r.getName().toLowerCase().contains("main"), "rosterEdit_" + name);
            guild.getConfigEditBuilder().addActionRow(selectMenu);
            guild.getConfigEditMessage()
                    .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();;
            InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("added field").queue();
            reply.deleteOriginal().queue();
        } else if (buttonUse.equalsIgnoreCase("editTrialRole")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("trial"),
                    "trialEdit_" + name);
            guild.getConfigEditBuilder().addActionRow(selectMenu);
            guild.getConfigEditMessage()
                    .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();;
                        InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("added field").queue();
            reply.deleteOriginal().queue();
        } else if (buttonUse.equalsIgnoreCase("editSubRole")) {
            guild.createEditingMessage();
            SelectMenu selectMenu = guild.getRoleSelectMenu((Role r) -> !r.getName().toLowerCase().contains("sub"),
                    "subEdit_" + name);
            guild.getConfigEditBuilder().addActionRow(selectMenu);
            guild.getConfigEditMessage()
                    .editMessage(MessageEditData.fromCreateData(guild.getConfigEditBuilder().build())).queue();;
                        InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("added field").queue();
            reply.deleteOriginal().queue();
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

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (!event.getModalId().split("_")[0].equalsIgnoreCase(name)) return;
        ModalMapping textInput = event.getValue("textInput");
        ModalMapping memberId = event.getValue("memberSelectMenu");
        ModalMapping roleId = event.getValue("roleSelectMenu");
        ModalMapping channelId = event.getValue("trialSelectMenu");
        try {
            if (textInput != null && event.getModalId().equalsIgnoreCase("editMinRank"))
                minRank = textInput.getAsString();
            if (textInput != null && event.getModalId().equalsIgnoreCase("editTeamUp"))
                teamupSubCalendar = Integer.parseInt(textInput.getAsString());
            if (memberId != null)
                manager = guild.getGuild().getMemberById(memberId.getAsString()).getUser();
            if (channelId != null && event.getModalId().equalsIgnoreCase("editTimeTable"))
                timetable = guild.getGuild().getTextChannelById(channelId.getAsString());
            if (channelId != null && event.getModalId().equalsIgnoreCase("editAnnouncement"))
                announcement = guild.getGuild().getTextChannelById(channelId.getAsString());
            if (roleId != null && event.getModalId().equalsIgnoreCase("editRosterRole"))
                rosterRole = guild.getGuild().getRoleById(roleId.getAsString());
            if (roleId != null && event.getModalId().equalsIgnoreCase("editTrialRole"))
                trialRole = guild.getGuild().getRoleById(roleId.getAsString());
            if (roleId != null && event.getModalId().equalsIgnoreCase("editSubRole"))
                subRole = guild.getGuild().getRoleById(roleId.getAsString());
        } catch (Exception e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
        updateConfigMessage();
        // guild.createConfig();
        event.reply("updated").setEphemeral(true).queue();
    }

    public LinkedList<Event> getEvents() {
        return events;
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
