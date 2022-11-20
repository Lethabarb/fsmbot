// package FSM.services;

// import java.time.Instant;
// import java.time.LocalDate;
// import java.time.ZoneOffset;
// import java.util.HashMap;
// import java.util.List;

// import javax.annotation.Nonnull;

// import FSM.entities.Event;
// import FSM.entities.Team;
// import net.dv8tion.jda.api.entities.Message;
// import net.dv8tion.jda.api.entities.MessageEmbed;
// import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
// import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
// import net.dv8tion.jda.api.events.GenericEvent;
// import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
// import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
// import net.dv8tion.jda.api.hooks.EventListener;
// import net.dv8tion.jda.api.hooks.ListenerAdapter;
// import net.dv8tion.jda.api.interactions.components.buttons.Button;
// import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
// import net.dv8tion.jda.api.utils.messages.MessageCreateData;
// import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
// import net.dv8tion.jda.api.utils.messages.MessageEditData;
// import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

// public class EventRepository extends ListenerAdapter {
//     private static EventRepository instance;
//     private HashMap<String, Event> events = new HashMap<>();

//     private EventRepository() {
//     }

//     public static EventRepository getInstance() {
//         if (instance == null) {
//             instance = new EventRepository();
//         }
//         return instance;
//     }

//     public synchronized void addEvent(Event event) {
//         events.put(event.getDate() + event.getTeam().getName(), event);
//     }

//     public Event getEvent(LocalDate date, String t) {
//         return events.get(date.toString() + t);
//     }

//     public Event getEvent(long unix, String t) {
//         return events.get(LocalDate
//                 .ofInstant(Instant.ofEpochSecond(unix), ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
//                 .toString() + t);
//     }

//     public boolean contains(LocalDate date, Team t) {
//         return events.get(date.toString() + t.getName()) != null;
//     }

//     @Override
//     public void onButtonInteraction(@Nonnull ButtonInteractionEvent buttonEvent) {
//         Button butt = buttonEvent.getButton();
//         buttonEvent.deferEdit();
//         DiscordBot bot = DiscordBot.getInstance(null);
//         if (butt.getId().equals("ButtonYes")) {
//             MessageEmbed embed = buttonEvent.getMessage().getEmbeds().get(0);
//             Footer footer = embed.getFooter();
//             String teamName = footer.getText();
//             String scrimUNIX = embed.getTitle().split(":")[1];
//             System.out.print("unix: " + scrimUNIX);
//             Event event = getEvent(Long.parseLong(scrimUNIX), teamName);
//             event.setMessageId(buttonEvent.getMessageId());
//             boolean partOfRoster = event.addConfirmedMember(buttonEvent.getMember().getUser().getName());
//             if (partOfRoster) {
//                 Message message = buttonEvent.getMessage();
//                 MessageEditData editData = bot.editEvent(message);
//                 buttonEvent.editMessage(editData).queue();
//                 bot.deleteSubRequest(event, buttonEvent.getMember());
//             }
//         } else if (butt.getId().equals("ButtonNo")) {
//             MessageEmbed embed = buttonEvent.getMessage().getEmbeds().get(0);
//             Footer footer = embed.getFooter();
//             String teamName = footer.getText();
//             String scrimUNIX = embed.getTitle().split(":")[1];
//             Event event = getEvent(Long.parseLong(scrimUNIX), teamName);
//             event.setMessageId(buttonEvent.getMessageId());
//             boolean partOfRoster = event.addDeclinedMember(buttonEvent.getMember().getUser().getName());
//             if (partOfRoster) {
//                 Message message = buttonEvent.getMessage();
//                 MessageEditData editData = bot.editEvent(message);
//                 buttonEvent.editMessage(editData).queue();
//                 if (event.addSubRequest(message, buttonEvent.getUser().getName())) {
//                     bot.sendSubRequest(event, buttonEvent.getMember());
//                 }
//             }
//         }

//         if (butt.getId().split("_")[0].equals("SubButton")) {
//             String unix = butt.getId().split("_")[2];
//             String author = buttonEvent.getMessage().getEmbeds().get(0).getAuthor().getName();
//             Event event = getEvent(Long.parseLong(unix),author);
//             boolean isOnTeam = event.addSubMember(buttonEvent.getUser().getName());
//             if (!isOnTeam) {
//                 Message message = bot.getMessage(event.getTeam().getBotChannel(), event.getMessageId());
//                 List<MessageEmbed> editData = bot.editEvent(message);
//                 message.editMessage(new MessageEditBuilder().setEmbeds(editData).build()).queue();
//                 buttonEvent.editButton(butt.asDisabled()).queue();
//                 bot.giveUserRole(buttonEvent.getMember(), event.getTeam().getSubRole());
//                 buttonEvent.reply("You are now subbing, please message staff if this is a mistake").setEphemeral(true).queue();
//             } else {
//                 buttonEvent.reply("you are already on the team :>").setEphemeral(true).queue();
//             }
//         }
//     }

//     @Override
//     public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {}
//     @Override
//     public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {}

// }
