package FSM.entities;

import java.lang.StackWalker.Option;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import FSM.services.DiscordBot;
import FSM.services.GoogleSheet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class Server {
    private static HashMap<Long, Server> repoos = new HashMap<>();
    private net.dv8tion.jda.api.entities.Guild guild;
    private MessageChannel subChannel;
    private HashMap<String, Team> teams = new HashMap<>();
    private Role subRole = null;
    private MessageChannel botConfigChannel;
    private boolean hasDuelSheetSetup = false;
    private boolean DifferentTeamSheetSetups = false;
    private GoogleSheet sheet;
    private SheetConfig sheetConfig;
    // private Role dpsRole = null;
    // private Role tankRole = null;
    // private Role suppRole = null;
    // OptionData eventOptions = new OptionData(OptionType.STRING, "events",
    // "unique event hash code that exists in the event footer");

    // public Server(MessageChannel botConfiChannel) {
    // this.botConfigChannel = botConfiChannel;

    // }

    public Server(Guild guild) {
        List<GuildChannel> channels = guild.getChannels();
        for (GuildChannel guildChannel : channels) {
            if (guildChannel.getName().equalsIgnoreCase("fsm-config")) {
                botConfigChannel = guild.getTextChannelById(guildChannel.getId());
            }
        }

    }

    public Server(Guild guild, MessageChannel subChannel, Role subRole, Team... teams) {
        System.out.println("Creating " + guild.getName() + " Server");
        this.guild = guild;
        this.subChannel = subChannel;
        // for (Team team : teams) {
        // this.teams.put(team.getName(), team);
        // }
        this.subRole = subRole;
        System.out.println("adding commands...");
        guild.updateCommands()
                .addCommands(
                        Commands.slash("update", "re-freshes an event details")
                                .addSubcommands(
                                        new SubcommandData("events", "updates all events in all servers").addOption(
                                                OptionType.ROLE, "teamrole", "role of the team to update events for")),
                        Commands.context(Type.MESSAGE, "edit"),
                        Commands.slash("role", "edit role of a player")
                                .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                                .addOption(OptionType.ROLE, "newplayerrole", "role to make the player"),
                        Commands.slash("sort", "sort the events of a channel"),
                        Commands.slash("removesubs", "removes the subs of a given team").addOption(OptionType.ROLE,
                                "teamrole", "the team main roster role", true))
                .queue();
        if (guild.getName().equalsIgnoreCase("flying spaghetti monster")) {
            System.out.println("adding config command");
            guild.updateCommands().addCommands(
                    Commands.slash("makeconfigchannel",
                            "sets the current channel for the guild to the bot config channel"),
                    Commands.slash("update", "re-freshes an event details")
                            .addSubcommands(new SubcommandData("events", "updates all events in all servers")
                                    .addOption(OptionType.ROLE, "teamrole", "role of the team to update events for")),
                    Commands.context(Type.MESSAGE, "edit responses"),
                    Commands.context(Type.MESSAGE, "edit details"),
                    Commands.slash("role", "edit role of a player")
                            .addOption(OptionType.MENTIONABLE, "playerdiscord", "Discord")
                            .addOption(OptionType.ROLE, "newplayerrole", "role to make the player"),
                    Commands.slash("sort", "sort the events of a channel"),
                    Commands.slash("removesubs", "removes the subs of a given team").addOption(OptionType.ROLE,
                            "teamrole", "the team main roster role", true))
                    .queue();
        }
        System.out.println("added commands");
        List<GuildChannel> channels = guild.getChannels();
        for (GuildChannel guildChannel : channels) {
            if (guildChannel.getName().equalsIgnoreCase("fsm-config"))
                ;
        }
        for (Team t : teams) {
            this.teams.put(t.getName(), t);
        }
        // this.dpsRole = dpsRole;
        // this.tankRole = tankRole;
        // this.suppRole = suppRole;
        // guild.upsertCommand("updatevent", "updates an event");
        // guild.upsertCommand("editevent", "updates an event");
        repoos.put(guild.getIdLong(), this);
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

    public boolean isHasDuelSheetSetup() {
        return hasDuelSheetSetup;
    }

    public void setHasDuelSheetSetup(boolean hasDuelSheetSetup) {
        this.hasDuelSheetSetup = hasDuelSheetSetup;
    }

    public boolean isDifferentTeamSheetSetups() {
        return DifferentTeamSheetSetups;
    }

    public void setDifferentTeamSheetSetups(boolean differentTeamSheetSetups) {
        DifferentTeamSheetSetups = differentTeamSheetSetups;
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
