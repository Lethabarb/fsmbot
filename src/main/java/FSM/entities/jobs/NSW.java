package FSM.entities.jobs;

import java.util.HashMap;

import FSM.services.jobs.ToDataService;

public class NSW implements ToDataService{
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

    private static HashMap<Integer, NSW> hashes = new HashMap<>();

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
    @Override
    public String[] toData() {
        return new String[] {Title, JobLocationText, AgencyName, JobUrl, ReferenceId, ClosingDate, ""};
        // TODO Auto-generated method stub
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ID;
        result = prime * result + ((Title == null) ? 0 : Title.hashCode());
        result = prime * result + ((PostedDate == null) ? 0 : PostedDate.hashCode());
        result = prime * result + ((ShortDescription == null) ? 0 : ShortDescription.hashCode());
        result = prime * result + ((ClosingDate == null) ? 0 : ClosingDate.hashCode());
        result = prime * result + AgencyId;
        result = prime * result + ((AgencyName == null) ? 0 : AgencyName.hashCode());
        result = prime * result + ((JobUrl == null) ? 0 : JobUrl.hashCode());
        result = prime * result + ((JobCategoryText == null) ? 0 : JobCategoryText.hashCode());
        result = prime * result + ((JobLocationText == null) ? 0 : JobLocationText.hashCode());
        result = prime * result + ((WorkType == null) ? 0 : WorkType.hashCode());
        result = prime * result + ((ReferenceId == null) ? 0 : ReferenceId.hashCode());
        result = prime * result + ((ApplyLink == null) ? 0 : ApplyLink.hashCode());
        result = prime * result + ((Sources == null) ? 0 : Sources.hashCode());
        result = prime * result + ((AgencyUrlKey == null) ? 0 : AgencyUrlKey.hashCode());
        result = prime * result + ((InternalJob == null) ? 0 : InternalJob.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NSW other = (NSW) obj;
        if (ID != other.ID)
            return false;
        if (Title == null) {
            if (other.Title != null)
                return false;
        } else if (!Title.equals(other.Title))
            return false;
        if (PostedDate == null) {
            if (other.PostedDate != null)
                return false;
        } else if (!PostedDate.equals(other.PostedDate))
            return false;
        if (ShortDescription == null) {
            if (other.ShortDescription != null)
                return false;
        } else if (!ShortDescription.equals(other.ShortDescription))
            return false;
        if (ClosingDate == null) {
            if (other.ClosingDate != null)
                return false;
        } else if (!ClosingDate.equals(other.ClosingDate))
            return false;
        if (AgencyId != other.AgencyId)
            return false;
        if (AgencyName == null) {
            if (other.AgencyName != null)
                return false;
        } else if (!AgencyName.equals(other.AgencyName))
            return false;
        if (JobUrl == null) {
            if (other.JobUrl != null)
                return false;
        } else if (!JobUrl.equals(other.JobUrl))
            return false;
        if (JobCategoryText == null) {
            if (other.JobCategoryText != null)
                return false;
        } else if (!JobCategoryText.equals(other.JobCategoryText))
            return false;
        if (JobLocationText == null) {
            if (other.JobLocationText != null)
                return false;
        } else if (!JobLocationText.equals(other.JobLocationText))
            return false;
        if (WorkType == null) {
            if (other.WorkType != null)
                return false;
        } else if (!WorkType.equals(other.WorkType))
            return false;
        if (ReferenceId == null) {
            if (other.ReferenceId != null)
                return false;
        } else if (!ReferenceId.equals(other.ReferenceId))
            return false;
        if (ApplyLink == null) {
            if (other.ApplyLink != null)
                return false;
        } else if (!ApplyLink.equals(other.ApplyLink))
            return false;
        if (Sources == null) {
            if (other.Sources != null)
                return false;
        } else if (!Sources.equals(other.Sources))
            return false;
        if (AgencyUrlKey == null) {
            if (other.AgencyUrlKey != null)
                return false;
        } else if (!AgencyUrlKey.equals(other.AgencyUrlKey))
            return false;
        if (InternalJob == null) {
            if (other.InternalJob != null)
                return false;
        } else if (!InternalJob.equals(other.InternalJob))
            return false;
        return true;
    }

    public boolean putinHash() {
        int hashcode = hashCode();
        NSW exist = hashes.get(hashcode);
        if (exist == null) {
            hashes.put(hashcode, this);
            return false;
        } else {
            return true;
        }
    }
}
