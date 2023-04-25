package FSM;

import FSM.entities.Server;
import FSM.entities.Team;
import FSM.entities.TeamDTO;
import FSM.services.DiscordBot;
import FSM.services.GoogleSheet;
import FSM.services.TeamUp;

public class Program {
    public static void main(String args[]) {
            
            // TODO: update sub working, update sheet working with new sys, add trials to
            // team
        DiscordBot bot = DiscordBot.getInstance(args[0]);
        TeamUp cal = TeamUp.getInstance(args[1]);
        // bot.test();
        // bot.test("bolognese bandits");
        // for (int i = 0; i < 20; i++) {
        //     bot.test("a".repeat(i));
        // }


        
        String bolName = "Bolognase Bandits";
        String bolNameAbbv = "Bol";
        String bolMinRank = "Masters5+";
        String bolTimetableId = "1026762361081696316";
        String bolRosterRoleId = "1026762517046886420";
        String bolTrialRoleId = "1026763075405230180";
        String bolSubRoleId = "1026763171115053066";
        int bolsubcal = 11997718;
        TeamDTO bolognaseBandits = new TeamDTO(bolName, bolNameAbbv, bolMinRank, bolTimetableId, bolRosterRoleId, bolTrialRoleId, bolSubRoleId, bolsubcal);
        
        String LingName = "Linguini Lords";
        String LingNameAbbv = "Ling";
        String LingMinRank = "GM1";
        String LingTimetableId = "968816107001638962";
        String LingRosterRoleId = "948413016976793661";
        String LingTrialRoleId = "952147100278325268";
        String LingSubRoleId = "948413021871566868";
        int LingSubCal = 11998119;
        
        TeamDTO LinguiniLords = new TeamDTO(LingName, LingNameAbbv, LingMinRank, LingTimetableId, LingRosterRoleId, LingTrialRoleId, LingSubRoleId, LingSubCal);
        
        String desName = "Ambition Desire";
        String desNameAbbv = "Des";
        String desMinRank = "Daimond3+";
        String desTimetableId = "1099949798838243388";
        String desRosterRoleId = "1099948306760749147";
        String desTrialRoleId = "1099948024844791918";
        String desSubRoleId = "1099948114510618727";
        int desSubCal = 11997719;

        TeamDTO abitionDesire = new TeamDTO(desName, desNameAbbv, desMinRank, desTimetableId, desRosterRoleId, desTrialRoleId, desSubRoleId, desSubCal);

        String fsmGuildId = "734267704516673536";
        String fsmSubChannelId = "824447819690672132";
        String fsmSubRoleId = "948413633182974032";

        Server fsm = bot.makeGuild(fsmGuildId, fsmSubChannelId, fsmSubRoleId, bolognaseBandits, LinguiniLords, abitionDesire);


        
        
        
        String ambitionGuildId = "883319891543867402";
        String ambitionSubChannelId = "900575082781503529";
        String ambitionSubRoleId = "883883773232562347";
        Server ambition = bot.makeGuild(ambitionGuildId, ambitionSubChannelId, ambitionSubRoleId, abitionDesire);
        // String andromedaGuildId = "861832597553152010";
        // String andromedaSubChannelId = "1039822109179908177";
        // String andromedaSubRoleId = "1039812765881225246";
        // Server andromeda = bot.makeGuild(andromedaGuildId, andromedaSubChannelId, andromedaSubRoleId);

        // String oberonName = "[ADR] Oberon";
        // String oberonNameAbbv = "Obr";
        // String oberonMinRank = "Daimond1+";
        // String oberonTimetableId = "982251123203252336";
        // String oberonRosterRoleId = "862202681945620481";
        // String oberonTrialRoleId = "862202608466919436";
        // String oberonSubRoleId = "862202822849724416";

        // Team oberon = bot.makeTeam(oberonName, oberonNameAbbv, oberonMinRank, oberonTimetableId, oberonRosterRoleId, oberonTrialRoleId, oberonSubRoleId, andromeda);

        // Server fsm = new Server("734267704516673536", "824447819690672132", "948413633182974032", "948413221235204116",
        //         "948413221629489252", "948413222006964275");
        // Server ambition = new Server("883319891543867402", "900575082781503529", "883871793818005534",
        //         "883871842476097589", "883871823807279135");

        // Team ravioli = new Team("Ravioli Rabbis", "1015001004720271552", "1014995273094791180", "Rav", "plat3+",
        //         "1014996001045614712", fsm);
        // Team bolognase = new Team("Bolognase Bandits", "1026762361081696316", "1026762517046886420", "Bol", "diamond4+",
        //         "1026763171115053066", fsm);
        // Team Desire = new Team("Ambition Desire", "923453836167905320", "883323099083317268", "Des", "diamond3+",
        //         "883323423915384934", ambition);
    }
}
