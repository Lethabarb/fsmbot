package FSM.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalField;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.iwebpp.crypto.TweetNaclFast.Hash;

import FSM.entities.Event;
import FSM.entities.SheetConfig;
import FSM.entities.Team;
import kotlin.collections.builders.ListBuilder;

public class GoogleSheet2 {
    private static Sheets sheet;
    private static String name = "Google Sheets API Java Quickstart";
    private static JsonFactory gson = GsonFactory.getDefaultInstance();
    private static String tokens = "tokens";
    private static List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static String credsPath = "/creds.json";
    private static boolean connected = false;
    private static String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };

    public GoogleSheet2() {
        if (!connected) {
            NetHttpTransport httpTransport = null;
            try {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
    }

    private static Credential getCreds(NetHttpTransport httpTransport) throws IOException {
        InputStream in = GoogleSheet2.class.getResourceAsStream(credsPath);
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

    public LinkedList<Event> getEvents(Team t) throws IOException {
        SheetConfig config = null;
        config = t.getSheetConfig();
        if (!t.getServer().isDifferentTeamSheetSetups() || config == null)
            config = t.getServer().getSheetConfig();

        if (config == null)
            throw new NullPointerException("team and server do not have a sheet config.");

        LinkedList<Event> events = new LinkedList<>();
        HashMap<String, String> eventMap = new HashMap<>();
        int numOfScrim = 0;
        int dateTimeParseExceptionCount = 0;

        if (config.getDirection().equalsIgnoreCase("right")) {
            int startRow = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String startColumn = String.valueOf(config.getStart().charAt(0));
            // HashMap<String, String> eventMapping = new HashMap<>();

            // count number of scrims
            LinkedList<LinkedList<String>> rawDataList = new LinkedList<>();
            int alpha = 0;
            while (!alphabet[alpha].equalsIgnoreCase(startColumn)) {
                alpha++;
            }
            System.out.println("alpha: " + alpha);
            String range = "'" + config.getSheetPage().replace("<NAME>", t.getNameAbbv()) + "'!" + config.getStart()
                    + ":"
                    + startColumn
                    + (startRow + 100);
            System.out.println(range);
            LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
            numOfScrim = vals.size();
            System.out.println("num of scrim: " + numOfScrim);

            // iterate over rows
            for (int i = 1; i < numOfScrim; i++) {
                if (config.getStep() > 0) {

                } else {
                    int row = startRow + i;
                    String lastCol = alphabet[alpha + config.getOrder().length - 1];
                    range = "'" + config.getSheetPage().replace("<NAME>", t.getNameAbbv()) + "'!" + startColumn + row
                            + ":"
                            + lastCol
                            + row;
                    vals = getValues(range, config.getSheetId());

                    /*
                     * Possible event map / order values
                     * Date, Time, DateTime, Team, Title, Type, Disc, Bnet
                     * 
                     * event inputs: String title, ZonedDateTime dateTime, Message message, String
                     * contact1, String contact2, Team team, int type
                     */

                    for (int cell = 0; cell < vals.getFirst().size(); cell++) {
                        // System.out.println(vals.getFirst().get(cell));
                        String key = config.getOrder()[cell];
                        String value = vals.getFirst().get(cell);
                        eventMap.put(key, value);
                    }

                }
                try {
                    Event e = createFromMapping(eventMap, config, t);
                    if (e != null && Event.getEvent(e.gethashCode()) == null) {
                        events.add(e);
                        t.addEvent(e);
                        Event.addEvent(e.gethashCode(), e);
                    }
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    dateTimeParseExceptionCount++;
                }
            }

        } else if (config.getDirection().equalsIgnoreCase("down")) {
            int orderLength = config.getOrder().length;
            int row = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String column = String.valueOf(config.getStart().charAt(0));
            int columnIndex = 0;
            int endRow = row + orderLength;
            boolean started = false;
            int dateRow = 0;
            while (!config.getOrder()[dateRow].equalsIgnoreCase("date")) {
                dateRow++;
            }
            while (!alphabet[columnIndex].equalsIgnoreCase(column)) {
                columnIndex++;
            }
            int stepCount = 0;
            while (!started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                if (vals.size() > 0) {
                    started = true;
                } else {
                    columnIndex++;
                    stepCount++;
                    if (stepCount >= config.getStep()) {
                        columnIndex -= config.getStep();
                        stepCount = 0;
                        row += 7;
                        endRow += 7;
                    }
                    column = alphabet[columnIndex];
                }
            }
            while (started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                System.out.println(range);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                System.out.println(vals.size());
                System.out.println(config.getOrder().length);
                if (vals.size() == config.getOrder().length) {
                    for (int i = 0; i < vals.size(); i++) {
                        eventMap.put(config.getOrder()[i], vals.get(i).getFirst());
                    }
                    try {
                        Event e = createFromMapping(eventMap, config, t);
                        if (e != null && Event.getEvent(e.gethashCode()) == null) {
                            events.add(e);
                            Event.addEvent(e.gethashCode(), e);
                        }
                    } catch (DateTimeParseException e) {
                        dateTimeParseExceptionCount++;
                        e.printStackTrace();
                    }
                } else if (vals.size() == 0) {
                    started = false;
                }
                columnIndex++;
                stepCount++;
                if (stepCount >= config.getStep()) {
                    columnIndex -= config.getStep();
                    stepCount = 0;
                    row += 7;
                    endRow += 7;
                }
                column = alphabet[columnIndex];
            }

        }

        if (dateTimeParseExceptionCount > 0) {
            t.getManager().openPrivateChannel().complete().sendMessage("could not read "
                    + dateTimeParseExceptionCount
                    + " dates and times in your sheet. Please ensure data is valid / all events have a date + time")
                    .queue();
        }
        return events;
    }

    public LinkedList<LinkedList<String>> getValues(String range, String sheetId) throws IOException {
        ValueRange vals = sheet.spreadsheets().values().get(sheetId, range).execute();
        List<List<Object>> grid = vals.getValues();
        LinkedList<LinkedList<String>> res = new LinkedList<>();
        try {
            for (List<Object> row : grid) {
                LinkedList<String> temp = new LinkedList<>();
                for (Object cell : row) {
                    temp.add((String) cell);
                }
                res.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("empty set");
            // TODO: handle exception
        }
        try {
            System.out.println("sleeping sheets api");
            Thread.sleep(500);
            System.out.println("waking");
        } catch (Exception e) {
            // TODO: handle exception
        }
        return res;
    }

    private Event createFromMapping(HashMap<String, String> eventMap, SheetConfig config, Team t)
            throws DateTimeParseException {
        String title;
        int type;
        if (config.isCombinedNameandType()) {
            title = eventMap.getOrDefault("Title", "scrim" + config.getTitleDelimiter() + "team")
                    .split(config.getTitleDelimiter())[1];

            System.out.println(eventMap.getOrDefault("Title", "scrim" + config.getTitleDelimiter() + "team")
                    .split(config.getTitleDelimiter())[0]);
            type = Event.typeHash(
                    eventMap.getOrDefault("Title", "scrim" + config.getTitleDelimiter() + "team")
                            .split(config.getTitleDelimiter())[0]);
        } else {
            title = eventMap.getOrDefault("Team", "Team");
            type = Event.typeHash(eventMap.getOrDefault("Type", "scrim"));
        }

        ZonedDateTime dt = null;
        String dateTime = eventMap.getOrDefault("DateTime", "");
        if (dateTime.equalsIgnoreCase("")) {
            String date = eventMap.getOrDefault("Date", "");
            String time = eventMap.getOrDefault("Time", "");
            if (date.equalsIgnoreCase("") || time.equalsIgnoreCase("")) {
                System.out.println("scrim has no dateTime???");
            } else {
                String formatString = config.getDateFormatter();
                DateTimeFormatter format = DateTimeFormatter.ofPattern(config.getDateFormatter(),
                        Locale.US);
                if (!formatString.contains("M")) {
                    formatString += "/M";
                    format = DateTimeFormatter.ofPattern(formatString);
                    date += LocalDateTime.now().format(DateTimeFormatter.ofPattern("/M"));
                }
                if (!formatString.contains("yy")) {
                    formatString += "/yy";
                    format = DateTimeFormatter.ofPattern(formatString);
                    date += LocalDateTime.now().format(DateTimeFormatter.ofPattern("/yy"));
                }
                // if (format.)
                // System.out.println(config.getDateFormatter());
                LocalDate ld = LocalDate.parse(date, format);
                formatString = config.getTimeFormatter();
                format = DateTimeFormatter.ofPattern(formatString, Locale.US);
                // System.out.println(config.getTimeFormatter());
                LocalTime lt;
                try {
                    lt = LocalTime.parse(time.toUpperCase(), format);
                } catch (Exception e) {
                    System.out.println("trying without mins");
                    formatString = formatString.replace(":mm", "");
                    System.out.println(formatString);
                    format = DateTimeFormatter.ofPattern(formatString, Locale.US);
                    lt = LocalTime.parse(time.toUpperCase(), format);
                    // TODO: handle exception
                }
                LocalDateTime ldt = LocalDateTime.of(ld, lt);
                dt = ldt.atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
            }
        } else {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(config.getDateTimeFormatter());
            dt = LocalDateTime.parse(dateTime, format)
                    .atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
        }

        String disc = eventMap.getOrDefault("Disc", "no Disc");
        String bnet = eventMap.getOrDefault("Bnet", "no bnet");

        if (dt != null
                && dt.isAfter(ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId()))) {
            Event e = new Event(title, dt, null, disc, bnet, t, type);

            return e;
        }
        return null;
    }

    private Event createFromMappingWithoutSending(HashMap<String, String> eventMap, SheetConfig config, Team t)
            throws DateTimeParseException {
        String title;
        int type;
        if (config.isCombinedNameandType()) {
            title = eventMap.getOrDefault("Title", "scrim" + config.getTitleDelimiter() + "team")
                    .split(config.getTitleDelimiter())[1];
            type = Event.typeHash(
                    eventMap.getOrDefault("Title", "scrim" + config.getTitleDelimiter() + "team")
                            .split(config.getTitleDelimiter())[0]);
        } else {
            title = eventMap.getOrDefault("Team", "Team");
            type = Event.typeHash(eventMap.getOrDefault("Type", "scrim"));
        }

        ZonedDateTime dt = null;
        String dateTime = eventMap.getOrDefault("DateTime", "");
        if (dateTime.equalsIgnoreCase("")) {
            String date = eventMap.getOrDefault("Date", "");
            String time = eventMap.getOrDefault("Time", "");
            if (date.equalsIgnoreCase("") || time.equalsIgnoreCase("")) {
                System.out.println("scrim has no dateTime???");
            } else {
                String formatString = config.getDateFormatter();
                DateTimeFormatter format = DateTimeFormatter.ofPattern(config.getDateFormatter(),
                        Locale.US);
                if (!formatString.contains("M")) {
                    formatString += "/M";
                    format = DateTimeFormatter.ofPattern(formatString);
                    date += LocalDateTime.now().format(DateTimeFormatter.ofPattern("/M"));
                }
                if (!formatString.contains("yy")) {
                    formatString += "/yy";
                    format = DateTimeFormatter.ofPattern(formatString);
                    date += LocalDateTime.now().format(DateTimeFormatter.ofPattern("/yy"));
                }
                // if (format.)
                // System.out.println(config.getDateFormatter());
                LocalDate ld = LocalDate.parse(date, format);
                formatString = config.getTimeFormatter();
                format = DateTimeFormatter.ofPattern(formatString, Locale.US);
                // System.out.println(config.getTimeFormatter());
                LocalTime lt;
                try {
                    lt = LocalTime.parse(time.toUpperCase(), format);
                } catch (Exception e) {
                    System.out.println("trying without mins");
                    formatString = formatString.replace(":mm", "");
                    System.out.println(formatString);
                    format = DateTimeFormatter.ofPattern(formatString, Locale.US);
                    lt = LocalTime.parse(time.toUpperCase(), format);
                    // TODO: handle exception
                }
                LocalDateTime ldt = LocalDateTime.of(ld, lt);
                dt = ldt.atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
            }
        } else {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(config.getDateTimeFormatter());
            dt = LocalDateTime.parse(dateTime, format)
                    .atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
        }

        String disc = eventMap.getOrDefault("Disc", "no Disc");
        String bnet = eventMap.getOrDefault("Bnet", "no bnet");

        if (dt != null
                && dt.isAfter(ZonedDateTime.now(TimeZone.getTimeZone("Australia/Sydney").toZoneId()))) {
            Event e = new Event(title, dt, disc, bnet, t, type);

            return e;
        }
        return null;
    }

    public void deleteEvent(Event e) throws IOException {
        SheetConfig config = null;
        config = e.getTeam().getSheetConfig();
        if (!e.getTeam().getServer().isDifferentTeamSheetSetups() || config == null)
            config = e.getTeam().getServer().getSheetConfig();

        if (config == null)
            throw new NullPointerException("team and server do not have a sheet config.");

        HashMap<String, String> eventMap = new HashMap<>();
        int numOfScrim = 0;
        int dateTimeParseExceptionCount = 0;

        if (config.getDirection().equalsIgnoreCase("right")) {
            int startRow = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String startColumn = String.valueOf(config.getStart().charAt(0));

            // get index of starting column
            LinkedList<LinkedList<String>> rawDataList = new LinkedList<>();
            int alpha = 0;
            while (!alphabet[alpha].equalsIgnoreCase(startColumn)) {
                alpha++;
            }

            // find number of scrims
            String range = "'" + config.getSheetPage().replace("<NAME>", e.getTeam().getNameAbbv()) + "'!"
                    + config.getStart() + ":"
                    + startColumn
                    + (startRow + 100);
            System.out.println(range);
            LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
            numOfScrim = vals.size();

            // iterate over rows
            for (int i = 1; i < numOfScrim; i++) {
                if (config.getStep() > 0) {

                } else {
                    int row = startRow + i;
                    String lastCol = alphabet[alpha + config.getOrder().length - 1];
                    range = "'" + config.getSheetPage().replace("<NAME>", e.getTeam().getNameAbbv()) + "'!"
                            + startColumn + row + ":"
                            + lastCol
                            + row;
                    vals = getValues(range, config.getSheetId());

                    /*
                     * Possible event map / order values
                     * Date, Time, DateTime, Team, Title, Type, Disc, Bnet
                     * 
                     * event inputs: String title, ZonedDateTime dateTime, Message message, String
                     * contact1, String contact2, Team team, int type
                     */

                    for (int cell = 0; cell < vals.getFirst().size(); cell++) {
                        String key = config.getOrder()[cell];
                        String value = vals.getFirst().get(cell);
                        eventMap.put(key, value);
                    }
                    try {
                        Event sheetEvent = createFromMappingWithoutSending(eventMap, config, e.getTeam());
                        if (sheetEvent != null && sheetEvent.compareTo(e) == 1) {
                            BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                            List<Sheet> pagesList = sheet.spreadsheets().get(config.getSheetId()).execute().getSheets();
                            Sheet page = null;
                            for (Sheet pageElement : pagesList) {
                                System.out.println(pageElement.getProperties().getTitle());
                                if (pageElement.getProperties().getTitle().equalsIgnoreCase(
                                        config.getSheetPage().replace("<NAME>", e.getTeam().getNameAbbv()))) {
                                    page = pageElement;
                                }
                            }
                            Request request = new Request()
                                    .setDeleteDimension(new DeleteDimensionRequest()
                                            .setRange(new DimensionRange()
                                                    .setSheetId(page.getProperties().getSheetId())
                                                    .setDimension("ROWS")
                                                    .setStartIndex(row - 1)
                                                    .setEndIndex(row)));
                            ListBuilder<Request> reqs = new ListBuilder<>();
                            reqs.add(request);
                            content.setRequests(reqs.build());
                            sheet.spreadsheets().batchUpdate(config.getSheetId(), content).execute();
                        }
                    } catch (DateTimeParseException exception) {
                        dateTimeParseExceptionCount++;
                    }

                }
            }

        } else if (config.getDirection().equalsIgnoreCase("down")) {
            int orderLength = config.getOrder().length;
            int row = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String column = String.valueOf(config.getStart().charAt(0));
            int columnIndex = 0;
            int endRow = row + orderLength;
            boolean started = false;
            int dateRow = 0;
            while (!config.getOrder()[dateRow].equalsIgnoreCase("date")) {
                dateRow++;
            }
            while (!alphabet[columnIndex].equalsIgnoreCase(column)) {
                columnIndex++;
            }
            int stepCount = 0;
            while (!started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                if (vals.size() > 0) {
                    started = true;
                } else {
                    columnIndex++;
                    stepCount++;
                    if (stepCount >= config.getStep()) {
                        columnIndex -= config.getStep();
                        stepCount = 0;
                        row += 7;
                        endRow += 7;
                    }
                    column = alphabet[columnIndex];
                }
            }
            while (started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                System.out.println(range);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                System.out.println(vals.size());
                System.out.println(config.getOrder().length);
                if (vals.size() == config.getOrder().length) {
                    for (int i = 0; i < vals.size(); i++) {
                        eventMap.put(config.getOrder()[i], vals.get(i).getFirst());
                    }
                    try {
                        Event sheetEvent = createFromMappingWithoutSending(eventMap, config, e.getTeam());
                        if (sheetEvent != null && sheetEvent.compareTo(e) == 1) {
                            Request request = new Request()
                                    .setDeleteDimension(new DeleteDimensionRequest()
                                            .setRange(new DimensionRange()
                                                    .setDimension("ROWS")
                                                    .setStartIndex(row)
                                                    .setEndIndex(row)));
                        }
                    } catch (DateTimeParseException ex) {
                        dateTimeParseExceptionCount++;
                        ex.printStackTrace();
                    }
                } else if (vals.size() == 0) {
                    started = false;
                }
                columnIndex++;
                stepCount++;
                if (stepCount >= config.getStep()) {
                    columnIndex -= config.getStep();
                    stepCount = 0;
                    row += 7;
                    endRow += 7;
                }
                column = alphabet[columnIndex];
            }

        }
    }

    public void updateEvent(Event newEvent, Event oldEvent) throws IOException {
        SheetConfig config = null;
        config = oldEvent.getTeam().getSheetConfig();
        if (!oldEvent.getTeam().getServer().isDifferentTeamSheetSetups() || config == null)
            config = oldEvent.getTeam().getServer().getSheetConfig();

        if (config == null)
            throw new NullPointerException("team and server do not have a sheet config.");

        LinkedList<Event> events = new LinkedList<>();
        HashMap<String, String> eventMap = new HashMap<>();
        int numOfScrim = 0;
        int dateTimeParseExceptionCount = 0;

        if (config.getDirection().equalsIgnoreCase("right")) {
            int startRow = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String startColumn = String.valueOf(config.getStart().charAt(0));
            // HashMap<String, String> eventMapping = new HashMap<>();

            // count number of scrims
            LinkedList<LinkedList<String>> rawDataList = new LinkedList<>();
            int alpha = 0;
            while (!alphabet[alpha].equalsIgnoreCase(startColumn)) {
                alpha++;
            }
            System.out.println("alpha: " + alpha);
            String range = "'" + config.getSheetPage().replace("<NAME>", oldEvent.getTeam().getNameAbbv()) + "'!"
                    + config.getStart() + ":"
                    + startColumn
                    + (startRow + 100);
            System.out.println(range);
            LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
            numOfScrim = vals.size();

            // iterate over rows
            for (int i = 1; i < numOfScrim; i++) {
                if (config.getStep() > 0) {

                } else {
                    int row = startRow + i;
                    String lastCol = alphabet[alpha + config.getOrder().length - 1];
                    range = "'" + config.getSheetPage().replace("<NAME>", oldEvent.getTeam().getNameAbbv()) + "'!"
                            + startColumn + row + ":"
                            + lastCol
                            + row;
                    vals = getValues(range, config.getSheetId());

                    /*
                     * Possible event map / order values
                     * Date, Time, DateTime, Team, Title, Type, Disc, Bnet
                     * 
                     * event inputs: String title, ZonedDateTime dateTime, Message message, String
                     * contact1, String contact2, Team team, int type
                     */

                    for (int cell = 0; cell < vals.getFirst().size(); cell++) {
                        String key = config.getOrder()[cell];
                        String value = vals.getFirst().get(cell);
                        eventMap.put(key, value);
                    }

                }
                try {
                    Event e = createFromMappingWithoutSending(eventMap, config, oldEvent.getTeam());
                    if (e != null && e.compareTo(oldEvent) == 1) {
                        setValues(range, config.getSheetId(), newEvent.toSheetValues());
                    }
                } catch (DateTimeParseException e) {
                    dateTimeParseExceptionCount++;
                }
            }

        } else if (config.getDirection().equalsIgnoreCase("down")) {
            int orderLength = config.getOrder().length;
            int row = Integer.parseInt(String.valueOf(config.getStart().charAt(1)));
            String column = String.valueOf(config.getStart().charAt(0));
            int columnIndex = 0;
            int endRow = row + orderLength;
            boolean started = false;
            int dateRow = 0;
            while (!config.getOrder()[dateRow].equalsIgnoreCase("date")) {
                dateRow++;
            }
            while (!alphabet[columnIndex].equalsIgnoreCase(column)) {
                columnIndex++;
            }
            int stepCount = 0;
            while (!started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                if (vals.size() > 0) {
                    started = true;
                } else {
                    columnIndex++;
                    stepCount++;
                    if (stepCount >= config.getStep()) {
                        columnIndex -= config.getStep();
                        stepCount = 0;
                        row += 7;
                        endRow += 7;
                    }
                    column = alphabet[columnIndex];
                }
            }
            while (started) {
                String range = String.format("'%s'!%s:%s", config.getSheetPage(), column + row, column + endRow);
                System.out.println(range);
                LinkedList<LinkedList<String>> vals = getValues(range, config.getSheetId());
                System.out.println(vals.size());
                System.out.println(config.getOrder().length);
                if (vals.size() == config.getOrder().length) {
                    for (int i = 0; i < vals.size(); i++) {
                        eventMap.put(config.getOrder()[i], vals.get(i).getFirst());
                    }
                    try {
                        Event e = createFromMappingWithoutSending(eventMap, config, oldEvent.getTeam());
                        if (e != null && Event.getEvent(e.gethashCode()) == null) {

                            // HERE

                        }
                    } catch (DateTimeParseException e) {
                        dateTimeParseExceptionCount++;
                        e.printStackTrace();
                    }
                } else if (vals.size() == 0) {
                    started = false;
                }
                columnIndex++;
                stepCount++;
                if (stepCount >= config.getStep()) {
                    columnIndex -= config.getStep();
                    stepCount = 0;
                    row += 7;
                    endRow += 7;
                }
                column = alphabet[columnIndex];
            }

        }
    }

    public void setValues(String range, String sheetId, List<List<Object>> vals) throws IOException {
        UpdateValuesResponse result = null;
        ValueRange body = new ValueRange()
                .setValues(vals);

        result = sheet.spreadsheets().values().update(sheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    // public static void main(String[] args) {
    // GoogleSheet2 sheet = new GoogleSheet2();
    // sheet.getCreds(null)
    // }
}
