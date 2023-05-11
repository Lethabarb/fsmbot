package FSM.entities;

public class TeamDTO {
    private String name;
    private String nameAbbv;
    private String minRank;
    private String timetableId;
    private String announceId;
    private String rosterRoleId;
    private String trialRoleId;
    private String subRoleId;
    private int subCalenderId;
    private String sheetId = "1HXcsb3Yt2tad_38UqIiAhFePZQ4-g-mMqIGYfLxnYcM";
    private String managerId;

    
    public TeamDTO(String name, String nameAbbv, String minRank, String timetableId, String announceId, String rosterRoleId,
    String trialRoleId, String subRoleId, int subCalenderId, String managerId) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetableId = timetableId;
        this.announceId = announceId;
        this.rosterRoleId = rosterRoleId;
        this.trialRoleId = trialRoleId;
        this.subRoleId = subRoleId;
        this.subCalenderId = subCalenderId;
        this.managerId = managerId;
    }
    
    public TeamDTO(String name, String nameAbbv, String minRank, String timetableId, String rosterRoleId,
    String trialRoleId, String subRoleId, int subCalenderId, String sheetId) {
        this.name = name;
        this.nameAbbv = nameAbbv;
        this.minRank = minRank;
        this.timetableId = timetableId;
        this.rosterRoleId = rosterRoleId;
        this.trialRoleId = trialRoleId;
        this.subRoleId = subRoleId;
        this.subCalenderId = subCalenderId;
        this.sheetId = sheetId;
    }
    
    // String name, String nameAbbv,
    // String minRank, String timetableId,
    // String rosterRoleId,
    // String trialRoleId,
    // String subRoleId, Server s,
    // int subCalenderId
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNameAbbv() {
        return nameAbbv;
    }

    public void setNameAbbv(String nameAbbv) {
        this.nameAbbv = nameAbbv;
    }
    
    public String getMinRank() {
        return minRank;
    }
    
    public void setMinRank(String minRank) {
        this.minRank = minRank;
    }
    
    public String getTimetableId() {
        return timetableId;
    }
    
    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }
    
    public String getRosterRoleId() {
        return rosterRoleId;
    }
    
    public void setRosterRoleId(String rosterRoleId) {
        this.rosterRoleId = rosterRoleId;
    }
    
    public String getTrialRoleId() {
        return trialRoleId;
    }
    
    public void setTrialRoleId(String trialRoleId) {
        this.trialRoleId = trialRoleId;
    }
    
    public String getSubRoleId() {
        return subRoleId;
    }
    
    public void setSubRoleId(String subRoleId) {
        this.subRoleId = subRoleId;
    }
    
    public int getSubCalenderId() {
        return subCalenderId;
    }
    
    public void setSubCalenderId(int subCalenderId) {
        this.subCalenderId = subCalenderId;
    }
    public String getSheetId() {
        return sheetId;
    }
    
    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getAnnounceId() {
        return announceId;
    }

    public void setAnnounceId(String announceId) {
        this.announceId = announceId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}
