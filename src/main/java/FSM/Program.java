package FSM;

import java.io.PrintStream;

import FSM.entities.Server;
import FSM.entities.SheetConfig;
import FSM.entities.Team;
import FSM.entities.TeamDTO;
import FSM.services.DiscordBot;
import FSM.services.DiscordOutputStream;
import FSM.services.GoogleSheet;
import FSM.services.GoogleSheet2;
import FSM.services.TeamUp;

public class Program {
        public static void main(String args[]) {

                // TODO: update sub working, update sheet working with new sys, add trials to
                // team
                // GoogleSheet2 sheet = new GoogleSheet2();
                DiscordBot bot = DiscordBot.getInstance(args[0]);
                TeamUp cal = TeamUp.getInstance("09bd8f9529db3c68e8c737d77592ecd0772ccf42efddacb7c66fe1923d8842a0");
                System.setErr(new DiscordOutputStream());

                // bot.test();
                // bot.test("bolognese bandits");
                // for (int i = 0; i < 20; i++) {
                // bot.test("a".repeat(i));
                // }

                String bolName = "Bolognase Bandits";
                String bolNameAbbv = "Team";
                String bolMinRank = "Masters5+";
                String bolTimetableId = "913380950119948318";
                String bolAnnounceId = "913380615129301032";
                String bolRosterRoleId = "913376182634971156";
                String bolTrialRoleId = "913380151595794443";
                String bolSubRoleId = "1102795133876965436";
                int bolsubcal = 12115557;
                String bolManager = "251578157822509057";
                // test team

                // TeamDTO bolognaseBandits = new TeamDTO(bolName, bolNameAbbv, bolMinRank, bolTimetableId, bolAnnounceId,
                //                 bolRosterRoleId, bolTrialRoleId, bolSubRoleId, bolsubcal, bolManager);

                String LingName = "Linguini Lords";
                String LingNameAbbv = "Ling";
                String LingMinRank = "GM1";
                String LingTimetableId = "968816107001638962";
                String LingAnnounceId = "967029608052559904";
                String LingRosterRoleId = "948413016976793661";
                String LingTrialRoleId = "952147100278325268";
                String LingSubRoleId = "948413021871566868";
                int LingSubCal = 11998119;

                TeamDTO LinguiniLords = new TeamDTO(LingName, LingNameAbbv, LingMinRank,
                LingTimetableId, LingAnnounceId,
                LingRosterRoleId, LingTrialRoleId, LingSubRoleId, LingSubCal, bolManager);

                String desName = "Ambition Desire";
                String desNameAbbv = "Des";
                String desMinRank = "Master5+";
                String desTimetableId = "1099949798838243388";
                String desAnnounceId = "1099949772397367387";
                String desRosterRoleId = "1099948306760749147";
                String desTrialRoleId = "1099948024844791918";
                String desSubRoleId = "1099948114510618727";
                int desSubCal = 11997719;

                TeamDTO abitionDesire = new TeamDTO(desName, desNameAbbv, desMinRank,
                desTimetableId, desAnnounceId,
                desRosterRoleId, desTrialRoleId, desSubRoleId, desSubCal, bolManager);

                String fsmGuildId = "734267704516673536";
                String fsmSubChannelId = "824447819690672132";
                String fsmSubRoleId = "948413633182974032";

                // test
                // String fsmGuildId = "913366063792685058";
                // String fsmSubChannelId = "913388820173561866";
                // String fsmSubRoleId = "1102795169201393674";

                String nathanServerGuildId = "1123577537436581908";
                String nathanServerSubChannelId = "1123826238771970199";
                String nathanServerSubRoleId = "1123827344319197195";

                String nathanTeamName = "Team";
                String nathanTeamAbbv = "Team";
                String nathanTeamMinRank = "Silver 5";
                String nathanTeamTimetableId = "1123603708186472571";
                String nathanTeamAnnounceId = "1123826016935219210";
                String nathanTeamRosterRoleId = "1123602824513720330";
                String nathanTeamTrialRoleId = "1123602881124257912";
                String nathanTeamSubRoleId = "1123827344319197195";
                String naughtswartUserId = "955260852427186186";

                TeamDTO nathanTeam = new TeamDTO(nathanTeamName, nathanTeamAbbv,
                nathanTeamMinRank, nathanTeamTimetableId, nathanTeamAnnounceId,
                nathanTeamRosterRoleId, nathanTeamTrialRoleId, nathanTeamSubRoleId,
                desSubCal, naughtswartUserId);

                String sheetId = "1HXcsb3Yt2tad_38UqIiAhFePZQ4-g-mMqIGYfLxnYcM";
                String sheetPage = "<NAME>_Event Input";
                String start = "B2";
                String direction = "right";
                int step = -1;
                boolean combinedNameandType = true;
                String titleDelimiter = " vs ";
                String[] order = { "Title", "Time", "Date", "Disc", "Bnet" };
                int eventSize = 3;
                String dateFormat = "d/M/yyyy";
                String timeFormat = "h:mm a";
                SheetConfig config = new SheetConfig(sheetId, sheetPage, start, direction, step, combinedNameandType,
                                titleDelimiter, order, eventSize, dateFormat, timeFormat);

                System.out.println(config.getSheetPage());

                Server fsm = bot.makeGuild(fsmGuildId, fsmSubChannelId, fsmSubRoleId, config,
                LinguiniLords, abitionDesire);
                Server nathanServer = bot.makeGuild(nathanServerGuildId,
                nathanServerSubChannelId, nathanTeamSubRoleId, config, nathanTeam);
                // Server fsm = bot.makeGuild(fsmGuildId, fsmSubChannelId, fsmSubRoleId, config, bolognaseBandits);
        }
}
