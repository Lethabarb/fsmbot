package FSM.entities;

import java.time.format.DateTimeFormatter;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class SheetConfig implements Cloneable {
    private static String[] possibleOrderCatagories = { "Date", "Time", "DateTime", "Team", "Type", "Disc", "Bnet",
            "PoC" };
    private String sheetId = "";
    private String sheetPage = ""; // <NAME> for team sheet inputs
    private String start = "";
    private String direction = "Right";
    private int step = 0; // -1 for no step
    private boolean combinedNameandType = false;
    private String titleDelimiter = "";
    private String[] order = {};
    private int eventSize = 0;
    private String dateTimeFormatter = "dd/MM/YYYY HH:mm a";
    private String dateFormatter = "dd/MM/YYYY";
    private String timeFormatter = "HH:mm a";

    public SheetConfig(String sheetId, String sheetPage, String start, String direction, int step,
            boolean combinedNameandType, String titleDelimiter, String[] order, int eventSize, String dateFormatter,
            String timeFormatter) {
        this.sheetId = sheetId;
        this.sheetPage = sheetPage;
        this.start = start;
        this.direction = direction;
        this.step = step;
        this.combinedNameandType = combinedNameandType;
        this.titleDelimiter = titleDelimiter;
        this.order = order;
        this.eventSize = eventSize;
        this.dateFormatter = dateFormatter;
        this.timeFormatter = timeFormatter;
        dateTimeFormatter = dateFormatter + timeFormatter;
    }
    public SheetConfig() {
    }

    public String getSheetId() {
        return sheetId;
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        return json;

    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetPage() {
        return sheetPage;
    }

    public void setSheetPage(String sheetPage) {
        this.sheetPage = sheetPage;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String[] getOrder() {
        return order;
    }

    public void setOrder(String[] order) {
        this.order = order;
    }

    public int getEventSize() {
        return eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
    }

    public boolean isCombinedNameandType() {
        return combinedNameandType;
    }

    public void setCombinedNameandType(boolean combinedNameandType) {
        this.combinedNameandType = combinedNameandType;
    }

    public String getTitleDelimiter() {
        return titleDelimiter;
    }

    public void setTitleDelimiter(String titleDelimiter) {
        this.titleDelimiter = titleDelimiter;
    }

    public static String[] getPossibleOrderCatagories() {
        return possibleOrderCatagories;
    }

    public static void setPossibleOrderCatagories(String[] possibleOrderCatagories) {
        SheetConfig.possibleOrderCatagories = possibleOrderCatagories;
    }

    public String getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(String dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public String getDateFormatter() {
        return dateFormatter;
    }

    public void setDateFormatter(String dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public String getTimeFormatter() {
        return timeFormatter;
    }

    public void setTimeFormatter(String timeFormatter) {
        this.timeFormatter = timeFormatter;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
