package FSM.entities;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import FSM.services.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class SubRequest extends ListenerAdapter {
    // private static HashMap<String, SubRequest> repos = new HashMap<>();
    private String uuid;
    private Player player = null;
    private Player trigger;
    private int subRole;
    private Message message = null;
    private Event event;

    public SubRequest(Player p, Event e) {
        this.trigger = p;
        this.subRole = p.getRole();
        event = e;
        uuid = UUID.randomUUID().toString();
        // repos.put(uuid, this);
    }

    public SubRequest(Player p, long e) {
        this.trigger = p;
        this.subRole = p.getRole();
        event = Event.getEvent(e);
        uuid = UUID.randomUUID().toString();
        // repos.put(uuid, this);
    }

    public SubRequest(Player trigger, Player sub, Event e) {
        this.trigger = trigger;
        this.player = sub;
        this.event = e;
        subRole = trigger.getRole();
        uuid = UUID.randomUUID().toString();
    }

    // public SubRequest(Player trigger, Player sub, Event e, String UUID) {
    //     this.trigger = trigger;
    //     this.player = sub;
    //     this.event = e;
    //     subRole = trigger.getRole();
    //     uuid = UUID;
    // }

    public SubRequest(String UUID, Event e, int role, Player trigger, Message m) {
        this.uuid = UUID;
        this.event = e;
        this.subRole = role;
        this.trigger = trigger;
        this.message = m;
        // e.addSubRequest(this);
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent buttonEvent) {
        String[] data = buttonEvent.getButton().getId().split("_");
        if (data[0].equalsIgnoreCase("sub") && data[1].equalsIgnoreCase(uuid)) {
            System.out.println("SUBREQ");
            InteractionHook reply = buttonEvent.deferReply(true).complete();
            reply.editOriginal("getting player details...").queue();
            Member member = buttonEvent.getMember();
            Player p = Player.getPlayer(member);
            if (p == null)
                reply.editOriginal("new player, creating...").queue();
                p = new Player(member);
                this.player = p;
            reply.editOriginal("checking roster...").queue();

            Boolean valid = true;
            for (Role r : member.getRoles()) {
                if (r.getId().equalsIgnoreCase(event.getTeam().getRosterRole().getId()) || r.getId().equalsIgnoreCase(event.getTeam().getTrialRole().getId())) {
                    valid = false;
                    reply.editOriginal("youre already on the roster, naughty!").queue();
                }
            }
            if (valid) {
                takeRequest();
                DiscordBot.getInstance().giveMemberRole(event.getTeam().getGuild().getGuild(), member, event.getTeam().getSubRole());
            }

            // message.delete().queue((res) -> {
            // System.out.println("sub button pressed, deleted sub message");
            // }, (res) -> {
            // System.out.println("sub button pressed, kept? sub message");
            // });
            event.updateEventMessage(DiscordBot.getInstance(), false);
        }
    }

    public String toString() {
        String res = "";
        if (player != null) {
            res += player.at;
            if (subRole == Player.TANK) {
                res += " - Tank";
            }
            if (subRole == Player.DPS) {
                res += " - DPS";
            }
            if (subRole == Player.SUPPORT) {
                res += " - Support";
            }
        } else {
            res += "TBA";
            if (subRole == Player.TANK) {
                res += " - Tank";
            }
            if (subRole == Player.DPS) {
                res += " - DPS";
            }
            if (subRole == Player.SUPPORT) {
                res += " - Support";
            }
        }
        if (trigger != null)
            res += " - " + trigger.at;
        return res += "\n";
    }

    public void takeRequest() {
        DiscordBot bot = DiscordBot.getInstance();
        String tankpng = "https://media.discordapp.net/attachments/740876905313599509/1044014909656137738/tank.png";
        String dpspng = "https://media.discordapp.net/attachments/740876905313599509/1044014917952475156/dps.png";
        String supportpng = "https://media.discordapp.net/attachments/740876905313599509/1044014928094298175/support.png";
        if (event.getType() == Event.COACHING)
            return;
        MessageCreateBuilder m = new MessageCreateBuilder();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);

        int width = 14;
        String lineDiv = bot.getEmoji("1043359277441618030");

        embed.setTitle("taken by " + player.getName());
        int remaining = width - event.getTeam().getName().length() / 3;
        Field teamField = new Field("```Team```", event.getTeam().getName(), true);
        Field dateTime = new Field("```date and Time```", "<t:" + event.getUnix() + ":F>", true);

        if (subRole == Player.TANK) {
            embed.setThumbnail(tankpng);
        }
        if (subRole == Player.DPS) {
            embed.setThumbnail(dpspng);
        }
        if (subRole == Player.SUPPORT) {
            embed.setThumbnail(supportpng);
        }
        embed.addField(teamField);
        embed.addField(dateTime);
        embed.setFooter(String.valueOf(event.gethashCode()));

        m.addEmbeds(embed.build());
        this.message.editMessage(MessageEditData.fromCreateData(m.build())).queue();
    }

    public void sendRequest() {
        DiscordBot bot = DiscordBot.getInstance();
        String tankpng = "https://media.discordapp.net/attachments/740876905313599509/1044014909656137738/tank.png";
        String dpspng = "https://media.discordapp.net/attachments/740876905313599509/1044014917952475156/dps.png";
        String supportpng = "https://media.discordapp.net/attachments/740876905313599509/1044014928094298175/support.png";
        if (event.getType() == Event.COACHING)
            return;
        MessageCreateBuilder m = new MessageCreateBuilder();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);

        int width = 14;
        String lineDiv = bot.getEmoji("1043359277441618030");

        embed.setTitle("**LFS** " + String.format("<t:%s:F>", event.getUnix()));
        int remaining = width - event.getTeam().getName().length() / 3;
        if (remaining % 2 == 0) {
            int sides = remaining / 2;
            embed.setDescription(
                    lineDiv.repeat(sides) + "**" + event.getTeam().getName() + "**" + lineDiv.repeat(sides));
        } else {
            int sides = remaining / 2;
            embed.setDescription(
                    lineDiv.repeat(sides) + "**" + event.getTeam().getName() + "**" + lineDiv.repeat(sides + 1));
        }
        Field rolefield = new Field("```Role```", "Tank", true);

        if (subRole == Player.TANK) {
            embed.setThumbnail(tankpng);
            rolefield = new Field("```Role```", "Tank", true);
        }
        if (subRole == Player.DPS) {
            embed.setThumbnail(dpspng);
            rolefield = new Field("```Role```", "DPS", true);
        }
        if (subRole == Player.SUPPORT) {
            embed.setThumbnail(supportpng);
            rolefield = new Field("```Role```", "Support", true);
        }
        embed.addField(rolefield);
        Field rank = new Field("```Rank```", event.getTeam().getMinRank(), true);
        embed.addField(rank);
        embed.setFooter(String.valueOf(event.gethashCode()));

        m.addEmbeds(embed.build());

        m.addActionRow(Button.primary(
                String.format("%s_%s", "Sub", uuid),
                "Sub "));
        event.getTeam().getGuild().getSubChannel().sendMessage(m.build()).queue((res) -> {
            System.out.println("sent sub req");
            message = res;
        }, (res) -> {
            System.out.println("didnt send sub request??");
        });
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message m) {
        this.message = m;
    }

    public int getSubRole() {
        return subRole;
    }

    public void setSubRole(int subRole) {
        this.subRole = subRole;
    }

    public void delete(String content) {
        // remove message
        if (message != null) {
            message.delete().queue((res) -> {
                System.out.println("deleted sub req message");
            }, (res) -> {
                System.out.println("couldnt delete sub req message");
            });
        }
        // remove player
        if (player != null) {
            player.getMember().getUser().openPrivateChannel().complete().sendMessage(content).queue();
            event.getTeam().getGuild().getGuild()
                    .removeRoleFromMember(player.getMember().getUser(), event.getTeam().getSubRole()).queue();
        }
    }

    // public static void deleteSubRequestsForEvent(Event e) {
    // LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
    // for (SubRequest req : requests) {
    // if (req.getEvent().compareTo(e) == 0) {
    // try {
    // req.message.delete().complete();

    // } catch (Exception ex) {
    // System.out.println("couldnt del scrim, continueing...");
    // }
    // }
    // }
    // }
    // public static SubRequest getRequest(String UUID) {
    // return repos.get(UUID);
    // }
    // public static SubRequest getRequestByRole(Event e, int role, boolean filled)
    // {
    // Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e)
    // != 0 && req.getSubRole() != role);
    // if (filled) pred = (SubRequest req) -> (req.getEvent().compareTo(e) != 0 &&
    // req.getSubRole() != role && req.getPlayer() != null);
    // LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
    // requests.removeIf(pred);
    // return requests.getFirst();
    // }
    // public static SubRequest getRequestByPlayer(Event e, Player p) {
    // Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e)
    // != 0 && req.getPlayer().equals(p));
    // LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
    // requests.removeIf(pred);
    // return requests.getFirst();
    // }
    // public static LinkedList<SubRequest> getRequestForEvent(Event e) {
    // Predicate<SubRequest> pred = (SubRequest req) -> (req.getEvent().compareTo(e)
    // != 0);
    // LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
    // requests.removeIf(pred);
    // return requests;
    // }
    // public boolean deleteRequest() {
    // return repos.remove(uuid, this);
    // }
    // public String getSubString(Event e) {
    // System.out.println("called getSubString for event " + e.getTitle());
    // String res = " ";
    // // Predicate<SubRequest> pred = (SubRequest req) ->
    // (req.getEvent().compareTo(e) != 0);
    // // LinkedList<SubRequest> requests = new LinkedList<>(repos.values());
    // // System.out.println(requests.size());
    // // requests.removeIf(pred);
    // for (int i = 0; i < requests.size(); i++) {
    // if (requests.get(i).getPlayer() != null) {
    // res += requests.get(i).getPlayer().at;
    // if (requests.get(i).getSubRole() == Player.TANK) {
    // res += " - Tank\n";
    // }
    // if (requests.get(i).getSubRole() == Player.DPS) {
    // res += " - DPS\n";
    // }
    // if (requests.get(i).getSubRole() == Player.SUPPORT) {
    // res += " - Support\n";
    // }
    // }
    // }
    // System.out.println(res);
    // return res;
    // }
    // public static HashMap<String, SubRequest> getRepos() {
    // return repos;
    // }
    // public static void setRepos(HashMap<String, SubRequest> repos) {
    // SubRequest.repos = repos;
    // }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Player getTrigger() {
        return trigger;
    }

    public void setTrigger(Player trigger) {
        this.trigger = trigger;
    }
}
