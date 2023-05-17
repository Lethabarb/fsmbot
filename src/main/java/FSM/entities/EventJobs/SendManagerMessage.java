package FSM.entities.EventJobs;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Predicate;

import FSM.entities.Event;
import FSM.entities.Team;
import FSM.services.EventJobRunner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class SendManagerMessage extends EventJob {
    private Team t;
    public SendManagerMessage(Team t, ZonedDateTime dt) {
        super(null, dt);
        this.t = t;

    }
    @Override
    public void action() {
        List<Event> events = t.getEvents();

        Predicate<Event> pred = (Event e) -> e.getDateTime().isAfter(timeToAction) && e.getDateTime().isBefore(timeToAction.plusDays(1));

        events.removeIf(pred);

        MessageCreateBuilder message = new MessageCreateBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(t.getName());

        for (Event e : events) {
            Field f = new Field(e.getTitle() + String.format(" <t:%s:t>", e.getUnix()), e.getDisc() + " - " + e.getBnet(), false);
            embed.addField(f);
        }
        message.addEmbeds(embed.build());
        t.getManager().openPrivateChannel().queue((res) -> {
            res.sendMessage(message.build()).queue();
        });
        SendManagerMessage next = new SendManagerMessage(t, timeToAction.plusDays(1));
        EventJobRunner.getInstance().addJob(next);
    }

    @Override
    public String toString() {
        return t.getName();
    }
    
}
