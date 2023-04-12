package FSM.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import FSM.entities.Event;

public class TeamUp {
    private static String apiKey;
    private static TeamUp instance = null;

    public TeamUp(String k) {
        apiKey = k;
    }

    public static TeamUp getInstance(String ... k) {
        if (instance == null) {
            instance = new TeamUp(k[0]);
        }
        return instance;
    }

    public static void main(String[] args) {
        LocalDateTime event = LocalDateTime.now();
        // ZonedDateTime event = dt.atZone(TimeZone.getTimeZone("Australia/Sydney").toZoneId());
        int offset = TimeZone.getTimeZone("Australia/Sydney").getOffset(0, event.getYear(),
        event.getMonthValue(), event.getDayOfMonth(),
        event.getDayOfWeek().getValue(), 0);
        offset /= 60000; // mins
        offset /= 60; //hours
        
        String startdt = event.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        System.out.println(startdt);
    }

    public boolean addCalenderEvent(Event event) {
        String subCalender = event.getTeam().getNameAbbv();
        String api = "https://api.teamup.com/";
        String calenderKey = "c7xv4s";
        // {
        // "subcalendar_ids": [
        // 11997718
        // ],
        // "subcalendar_remote_ids": [
        // null
        // ],
        // "start_dt": "2023-03-29T14:00:00+11:00",
        // "end_dt": "2023-03-29T15:00:00+11:00",
        // "all_day": false,
        // "tz": "Australia/Sydney",
        // "title": "Scrim vs x"
        // }
        JsonObject data = new JsonObject();
        JsonArray subcal = new JsonArray(1);
        subcal.add(new JsonPrimitive(event.getTeam().getTeamupSubCalendar()));
        data.add("subcalendar_ids", subcal);
        int offset = TimeZone.getTimeZone("Australia/Sydney").getOffset(0, event.getDateTime().getYear(),
        event.getDateTime().getMonthValue(), event.getDateTime().getDayOfMonth(),
        event.getDateTime().getDayOfWeek().getValue(), 0);
        offset /= 60000; // mins
        offset /= 60; //hours
        
        String startdt = event.getDateTime().format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'+'Z"));
        System.out.println(startdt);
        String enddt = event.getDateTime().plusHours(2).format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'+'Z"));
        data.add("start_dt", new JsonPrimitive(startdt));
        data.add("end_dt", new JsonPrimitive(startdt));
        data.add("all_day", new JsonPrimitive(false));
        data.add("tz", new JsonPrimitive("Australia/Sydney"));
        // data.add("subcalendar_ids", subcal);
        String[] types = { "Scrim", "AAOL", "Coaching", "Open Div" };
        if (event.getType() == 2) {
            data.add("title", new JsonPrimitive(types[event.getType()] + " with " + event.getTitle()));

        } else {
            data.add("title", new JsonPrimitive(types[event.getType()] + " vs " + event.getTitle()));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.teamup.com/ksfigdh97kfun3yo4q/events?inputFormat=html"))
                .header("Content-Type", "application/json")
                .header("Teamup-Token", apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(data.toString()))
                        // "{\n  \"subcalendar_ids\": [\n    11997718\n  ],\n  \"subcalendar_remote_ids\": [\n    null\n  ],\n  \"start_dt\": \"2023-03-29T14:00:00+11:00\",\n  \"end_dt\": \"2023-03-29T15:00:00+11:00\",\n  \"all_day\": false,\n  \"tz\": \"Australia/Sydney\",\n  \"title\": \"Scrim vs x\"\n}"))
                .build();
        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;

    }
}
