package FSM.entities.jobs;

import java.util.Arrays;
import java.util.HashMap;

import FSM.services.jobs.ToDataService;

public class VIC implements ToDataService {
    private int ID;
    private String Title;
    private String PostedDate;
    private String ShortDescription;
    private String ClosingDate;
    private int AgencyID;
    private String AgencyName;
    private String AgencyUrl;
    private String JobUrl;
    private String JobCategoryTex;
    private String JobLocationText;
    private String WorkType;
    private String ReferenceID;
    private String ApplyLink;
    private String Sources;
    private String AgencyUrlKey;
    private String InternalJob;
    private boolean Enhanced;
    private String AgencyLogo;
    private String AgencyEnhancedDescription;
    private String PostingState;
    private boolean IsBestEmployer;
    private boolean IsAdHoc;
    private String ADLink;
    private boolean IsFavouriteJob;
    private boolean IsNewJob;
    private int CountDay;
    private String[] listWorkType;
    private String[] Categories;
    private String[] Classifications;
    private String[] Locations;
    private String LogoALT;
    private String JobDuration;
    private String SalaryInformation;
    private String JobLocationMobile;

    private static HashMap<Integer, VIC> hashes = new HashMap<>();
    @Override
    public String[] toData() {
        return new String[] {Title, JobLocationText, AgencyName, JobUrl, ReferenceID, ClosingDate, SalaryInformation};
        // throw new UnsupportedOperationException("Unimplemented method 'toData'");
    }
    @Override
    public boolean putinHash() {
        int hashcode = hashCode();
        VIC exist = hashes.get(hashcode);
        if (exist == null) {
            hashes.put(hashcode, this);
            return false;
        } else {
            return true;
        }
    }
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
    public int getAgencyID() {
        return AgencyID;
    }
    public void setAgencyID(int agencyID) {
        AgencyID = agencyID;
    }
    public String getAgencyName() {
        return AgencyName;
    }
    public void setAgencyName(String agencyName) {
        AgencyName = agencyName;
    }
    public String getAgencyUrl() {
        return AgencyUrl;
    }
    public void setAgencyUrl(String agencyUrl) {
        AgencyUrl = agencyUrl;
    }
    public String getJobUrl() {
        return JobUrl;
    }
    public void setJobUrl(String jobUrl) {
        JobUrl = jobUrl;
    }
    public String getJobCategoryTex() {
        return JobCategoryTex;
    }
    public void setJobCategoryTex(String jobCategoryTex) {
        JobCategoryTex = jobCategoryTex;
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
    public String getReferenceID() {
        return ReferenceID;
    }
    public void setReferenceID(String referenceID) {
        ReferenceID = referenceID;
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
    public boolean isEnhanced() {
        return Enhanced;
    }
    public void setEnhanced(boolean enhanced) {
        Enhanced = enhanced;
    }
    public String getAgencyLogo() {
        return AgencyLogo;
    }
    public void setAgencyLogo(String agencyLogo) {
        AgencyLogo = agencyLogo;
    }
    public String getAgencyEnhancedDescription() {
        return AgencyEnhancedDescription;
    }
    public void setAgencyEnhancedDescription(String agencyEnhancedDescription) {
        AgencyEnhancedDescription = agencyEnhancedDescription;
    }
    public String getPostingState() {
        return PostingState;
    }
    public void setPostingState(String postingState) {
        PostingState = postingState;
    }
    public boolean isIsBestEmployer() {
        return IsBestEmployer;
    }
    public void setIsBestEmployer(boolean isBestEmployer) {
        IsBestEmployer = isBestEmployer;
    }
    public boolean isIsAdHoc() {
        return IsAdHoc;
    }
    public void setIsAdHoc(boolean isAdHoc) {
        IsAdHoc = isAdHoc;
    }
    public String getADLink() {
        return ADLink;
    }
    public void setADLink(String aDLink) {
        ADLink = aDLink;
    }
    public boolean isIsFavouriteJob() {
        return IsFavouriteJob;
    }
    public void setIsFavouriteJob(boolean isFavouriteJob) {
        IsFavouriteJob = isFavouriteJob;
    }
    public boolean isIsNewJob() {
        return IsNewJob;
    }
    public void setIsNewJob(boolean isNewJob) {
        IsNewJob = isNewJob;
    }
    public int getCountDay() {
        return CountDay;
    }
    public void setCountDay(int countDay) {
        CountDay = countDay;
    }
    public String[] getListWorkType() {
        return listWorkType;
    }
    public void setListWorkType(String[] listWorkType) {
        this.listWorkType = listWorkType;
    }
    public String[] getCategories() {
        return Categories;
    }
    public void setCategories(String[] categories) {
        Categories = categories;
    }
    public String[] getClassifications() {
        return Classifications;
    }
    public void setClassifications(String[] classifications) {
        Classifications = classifications;
    }
    public String[] getLocations() {
        return Locations;
    }
    public void setLocations(String[] locations) {
        Locations = locations;
    }
    public String getLogoALT() {
        return LogoALT;
    }
    public void setLogoALT(String logoALT) {
        LogoALT = logoALT;
    }
    public String getJobDuration() {
        return JobDuration;
    }
    public void setJobDuration(String jobDuration) {
        JobDuration = jobDuration;
    }
    public String getSalaryInformation() {
        return SalaryInformation;
    }
    public void setSalaryInformation(String salaryInformation) {
        SalaryInformation = salaryInformation;
    }
    public String getJobLocationMobile() {
        return JobLocationMobile;
    }
    public void setJobLocationMobile(String jobLocationMobile) {
        JobLocationMobile = jobLocationMobile;
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
        result = prime * result + AgencyID;
        result = prime * result + ((AgencyName == null) ? 0 : AgencyName.hashCode());
        result = prime * result + ((AgencyUrl == null) ? 0 : AgencyUrl.hashCode());
        result = prime * result + ((JobUrl == null) ? 0 : JobUrl.hashCode());
        result = prime * result + ((JobCategoryTex == null) ? 0 : JobCategoryTex.hashCode());
        result = prime * result + ((JobLocationText == null) ? 0 : JobLocationText.hashCode());
        result = prime * result + ((WorkType == null) ? 0 : WorkType.hashCode());
        result = prime * result + ((ReferenceID == null) ? 0 : ReferenceID.hashCode());
        result = prime * result + ((ApplyLink == null) ? 0 : ApplyLink.hashCode());
        result = prime * result + ((Sources == null) ? 0 : Sources.hashCode());
        result = prime * result + ((AgencyUrlKey == null) ? 0 : AgencyUrlKey.hashCode());
        result = prime * result + ((InternalJob == null) ? 0 : InternalJob.hashCode());
        result = prime * result + (Enhanced ? 1231 : 1237);
        result = prime * result + ((AgencyLogo == null) ? 0 : AgencyLogo.hashCode());
        result = prime * result + ((AgencyEnhancedDescription == null) ? 0 : AgencyEnhancedDescription.hashCode());
        result = prime * result + ((PostingState == null) ? 0 : PostingState.hashCode());
        result = prime * result + (IsBestEmployer ? 1231 : 1237);
        result = prime * result + (IsAdHoc ? 1231 : 1237);
        result = prime * result + ((ADLink == null) ? 0 : ADLink.hashCode());
        result = prime * result + (IsFavouriteJob ? 1231 : 1237);
        result = prime * result + (IsNewJob ? 1231 : 1237);
        result = prime * result + CountDay;
        result = prime * result + Arrays.hashCode(listWorkType);
        result = prime * result + Arrays.hashCode(Categories);
        result = prime * result + Arrays.hashCode(Classifications);
        result = prime * result + Arrays.hashCode(Locations);
        result = prime * result + ((LogoALT == null) ? 0 : LogoALT.hashCode());
        result = prime * result + ((JobDuration == null) ? 0 : JobDuration.hashCode());
        result = prime * result + ((SalaryInformation == null) ? 0 : SalaryInformation.hashCode());
        result = prime * result + ((JobLocationMobile == null) ? 0 : JobLocationMobile.hashCode());
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
        VIC other = (VIC) obj;
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
        if (AgencyID != other.AgencyID)
            return false;
        if (AgencyName == null) {
            if (other.AgencyName != null)
                return false;
        } else if (!AgencyName.equals(other.AgencyName))
            return false;
        if (AgencyUrl == null) {
            if (other.AgencyUrl != null)
                return false;
        } else if (!AgencyUrl.equals(other.AgencyUrl))
            return false;
        if (JobUrl == null) {
            if (other.JobUrl != null)
                return false;
        } else if (!JobUrl.equals(other.JobUrl))
            return false;
        if (JobCategoryTex == null) {
            if (other.JobCategoryTex != null)
                return false;
        } else if (!JobCategoryTex.equals(other.JobCategoryTex))
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
        if (ReferenceID == null) {
            if (other.ReferenceID != null)
                return false;
        } else if (!ReferenceID.equals(other.ReferenceID))
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
        if (Enhanced != other.Enhanced)
            return false;
        if (AgencyLogo == null) {
            if (other.AgencyLogo != null)
                return false;
        } else if (!AgencyLogo.equals(other.AgencyLogo))
            return false;
        if (AgencyEnhancedDescription == null) {
            if (other.AgencyEnhancedDescription != null)
                return false;
        } else if (!AgencyEnhancedDescription.equals(other.AgencyEnhancedDescription))
            return false;
        if (PostingState == null) {
            if (other.PostingState != null)
                return false;
        } else if (!PostingState.equals(other.PostingState))
            return false;
        if (IsBestEmployer != other.IsBestEmployer)
            return false;
        if (IsAdHoc != other.IsAdHoc)
            return false;
        if (ADLink == null) {
            if (other.ADLink != null)
                return false;
        } else if (!ADLink.equals(other.ADLink))
            return false;
        if (IsFavouriteJob != other.IsFavouriteJob)
            return false;
        if (IsNewJob != other.IsNewJob)
            return false;
        if (CountDay != other.CountDay)
            return false;
        if (!Arrays.equals(listWorkType, other.listWorkType))
            return false;
        if (!Arrays.equals(Categories, other.Categories))
            return false;
        if (!Arrays.equals(Classifications, other.Classifications))
            return false;
        if (!Arrays.equals(Locations, other.Locations))
            return false;
        if (LogoALT == null) {
            if (other.LogoALT != null)
                return false;
        } else if (!LogoALT.equals(other.LogoALT))
            return false;
        if (JobDuration == null) {
            if (other.JobDuration != null)
                return false;
        } else if (!JobDuration.equals(other.JobDuration))
            return false;
        if (SalaryInformation == null) {
            if (other.SalaryInformation != null)
                return false;
        } else if (!SalaryInformation.equals(other.SalaryInformation))
            return false;
        if (JobLocationMobile == null) {
            if (other.JobLocationMobile != null)
                return false;
        } else if (!JobLocationMobile.equals(other.JobLocationMobile))
            return false;
        return true;
    }
}
