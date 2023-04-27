package FSM.entities;

public class SheetConfig {
    private String sheetId;
    private String sheetPage;
    private String start;
    private String direction;
    private int step; // -1 for no step
    private String[] order;
    private int eventSize;
    public SheetConfig(String sheetId, String sheetPage, String start, String direction, int step, String[] order,
            int eventSize) {
        this.sheetId = sheetId;
        this.sheetPage = sheetPage;
        this.start = start;
        this.direction = direction;
        this.step = step;
        this.order = order;
        this.eventSize = eventSize;
    }
    public String getSheetId() {
        return sheetId;
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
}
