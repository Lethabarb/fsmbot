package FSM.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import FSM.entities.Event;
import FSM.entities.Team;

public class GoogleSheet {
    private static String name = "Google Sheets API Java Quickstart";
    private static JsonFactory gson = GsonFactory.getDefaultInstance();
    private static String tokens = "tokens";
    private static List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static String credsPath = "/creds.json";
    private boolean connected = false;

    private String sheetId = "1HXcsb3Yt2tad_38UqIiAhFePZQ4-g-mMqIGYfLxnYcM";
    private Sheets sheet;

    public GoogleSheet() {
        NetHttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sheet = new Sheets.Builder(httpTransport, gson, getCreds(httpTransport))
                    .setApplicationName("FSM BOT")
                    .build();
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<LinkedList<String>> getValues(String range) throws IOException {
        ValueRange vals = sheet.spreadsheets().values().get(sheetId, range).execute();
        List<List<Object>> grid = vals.getValues();
        LinkedList<LinkedList<String>> res = new LinkedList<>();

        for (List<Object> row : grid) {
            LinkedList<String> temp = new LinkedList<>();
            for (Object cell : row) {
                temp.add((String) cell);
            }
            res.add(temp);
        }
        return res;
    }

    public void setValues(String range, List<List<Object>> vals) throws IOException {
        UpdateValuesResponse result = null;
        ValueRange body = new ValueRange()
                .setValues(vals);

        result = sheet.spreadsheets().values().update(sheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private static Credential getCreds(NetHttpTransport httpTransport) throws IOException {
        InputStream in = GoogleSheet.class.getResourceAsStream(credsPath);
        if (in == null)
            throw new FileNotFoundException("creds");

        GoogleClientSecrets secrets = GoogleClientSecrets.load(gson, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, gson, secrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokens)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public synchronized LinkedList<Event> getEvents(String teamName, Team t) throws IOException {
        String page = String.format("'%s_Schedule'!", teamName);
        int numOfScrims = Integer.parseInt(getValues(page + "A1:A1").get(0).get(0));
        LinkedList<Event> events = new LinkedList<>();
        for (int i = 0; i < numOfScrims; i++) {
            LinkedList<LinkedList<String>> vals = getValues(String.format("%sB%s:C%s",page, 2 + (i * 5), 5 + (i * 5)));
            int a = 0;
            int b = 0;
            for (LinkedList<String> ll : vals) {
                for (String s : ll)  {
                    System.out.println(String.format("[%s][%s] = %s", a, b, s));
                    b++;
                }
                a++;
                b = 0;
            }
            String title = vals.get(0).get(0);
            String typeString = title.split(" vs ")[0];
            if (!title.equalsIgnoreCase("#N/A"))
                title = title.split(" vs ")[1];
            String date = vals.get(1).get(0) + "/2023";
            String time = vals.get(1).get(1);
            String disc = vals.get(3).get(0);
            String bnet = vals.get(3).get(1);
            int type = 0;
            switch (typeString) {
                case "Scrim":
                    type = Event.SCRIM;
                    break;
                case "AAOL":
                    type = Event.AAOL;
                    break;
                case "OD":
                    type = Event.OPENDIV;
                    break;
                case "Coaching":
                    type = Event.COACHING;
                    break;
                default:
                    break;
            }

            if (!title.equals("#N/A")) {
                Event e = new Event(title, LocalDateTime.parse(time + date + "", DateTimeFormatter.ofPattern("h:mm a zE dd/MM/yyyy",Locale.US)), null, disc, bnet, t, type);
                events.add(e);
            }
        }
        return events;

    }

    // public static void main(String[] args) {
    //     LocalDateTime dt1 = LocalDateTime.parse("7:30 PM AESTMon 09/01/2023", DateTimeFormatter.ofPattern("h:mm a zE dd/MM/yyyy",Locale.US));
    //     LocalDateTime dt2 = LocalDateTime.parse("7:30 PM AEDTMon 09/01/2023", DateTimeFormatter.ofPattern("h:mm a zE dd/MM/yyyy",Locale.US));
    //     LocalDateTime dt3 = LocalDateTime.parse("7:30 PM GSTMon 09/01/2023", DateTimeFormatter.ofPattern("h:mm a zE dd/MM/yyyy",Locale.US));
    //     System.out.println(dt1.toEpochSecond(ZoneOffset.of("+11")));
    //     System.out.println(dt2.toEpochSecond(ZoneOffset.of("+11")));
    //     System.out.println(dt3.toEpochSecond(ZoneOffset.of("+11")));
    // }

    public void updateEvent(String teamname, Event event) {
        String[] types = { "Scrim", "AAOL", "Coaching", "Open Divison" };
        String page = teamname + "_Event Input";
        LinkedList<Object> vals = new LinkedList<>();
        String fulltitle = String.format("%s vs %s", types[event.getType()], event.getTitle());
        vals.add(fulltitle);
        vals.add(event.getDateTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)));
        System.out.print("'"+event.getDateTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))+"'");
        vals.add(event.getDateTime().format(DateTimeFormatter.ofPattern("MM/dd/YY", Locale.US)));
        vals.add(event.getContact1());
        vals.add(event.getContact2());

        try {
            LinkedList<LinkedList<String>> numOfScrims = getValues(String.format("'%s'!A1:A1", page));
            int numScrims = Integer.parseInt(numOfScrims.getFirst().getFirst()) + 2;
            LinkedList<LinkedList<String>> scrims = getValues(String.format("'%s'!B2:B%s", page, numScrims));
            int c = 0;
            for (int i = 0; i < scrims.size(); i++) {
                System.out.println(String.format("%s == %s", fulltitle, scrims.get(i).getFirst()));
                if (scrims.get(i).getFirst().equals(fulltitle)) {
                    c = i + 2;
                }
            }

            String range = String.format("'%s'!B%s:F%s", page,c,c);
            List<List<Object>> vvals = new LinkedList<>();
            vvals.add(vals);
            setValues(range, vvals);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}
