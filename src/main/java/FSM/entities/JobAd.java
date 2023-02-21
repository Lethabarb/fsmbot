package FSM.entities;

public class JobAd {
    private int ID;
    private String Title;
    private String PostedDate;
    private String ShortDescription;
    private String ClosingDate;
    private int AgencyId;
    private String AgencyName;
    private String JobUrl;
    private String JobCategoryText;
    private String JobLocationText;
    private String WorkType;
    private String ReferenceId;
    private String ApplyLink;
    private String Sources;
    private String AgencyUrlKey;
    private String InternalJob;

    public int getID() {
        return ID;
    }
    public void setID(int iD) {
        ID = iD;
    }
    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public String getPostedDate() {
        return PostedDate;
    }
    public void setPostedDate(String postedDate) {
        PostedDate = postedDate;
    }
    public String getShortDescription() {
        return ShortDescription;
    }
    public void setShortDescription(String shortDescription) {
        ShortDescription = shortDescription;
    }
    public String getClosingDate() {
        return ClosingDate;
    }
    public void setClosingDate(String closingDate) {
        ClosingDate = closingDate;
    }
    public int getAgencyId() {
        return AgencyId;
    }
    public void setAgencyId(int agencyId) {
        AgencyId = agencyId;
    }
    public String getAgencyName() {
        return AgencyName;
    }
    public void setAgencyName(String agencyName) {
        AgencyName = agencyName;
    }
    public String getJobUrl() {
        return JobUrl;
    }
    public void setJobUrl(String jobUrl) {
        JobUrl = jobUrl;
    }
    public String getJobCategoryText() {
        return JobCategoryText;
    }
    public void setJobCategoryText(String jobCategoryText) {
        JobCategoryText = jobCategoryText;
    }
    public String getJobLocationText() {
        return JobLocationText;
    }
    public void setJobLocationText(String jobLocationText) {
        JobLocationText = jobLocationText;
    }
    public String getWorkType() {
        return WorkType;
    }
    public void setWorkType(String workType) {
        WorkType = workType;
    }
    public String getReferenceId() {
        return ReferenceId;
    }
    public void setReferenceId(String referenceId) {
        ReferenceId = referenceId;
    }
    public String getApplyLink() {
        return ApplyLink;
    }
    public void setApplyLink(String applyLink) {
        ApplyLink = applyLink;
    }
    public String getSources() {
        return Sources;
    }
    public void setSources(String sources) {
        Sources = sources;
    }
    public String getAgencyUrlKey() {
        return AgencyUrlKey;
    }
    public void setAgencyUrlKey(String agencyUrlKey) {
        AgencyUrlKey = agencyUrlKey;
    }
    public String getInternalJob() {
        return InternalJob;
    }
    public void setInternalJob(String internalJob) {
        InternalJob = internalJob;
    }
}
