package FSM.entities.jobs;

import java.util.HashMap;

import com.google.gson.JsonArray;

import FSM.services.jobs.ToDataService;

public class NT implements ToDataService {
    private int rtfId;
    private String positionNumber;
    private String jobTitle;
    private String vacancyType;
    private String altVacancyType;
    private String numberOfVacancies;
    private String vacancyTypeCodeValue;
    private String agency;
    private String section;
    private String contactPerson;
    private String primaryObjective;
    private String advertisingType;
    private String applicationType;
    private String specialInstructions;
    private String closingDate;
    private String locations;
    private String designations;
    private boolean isCanceled;
    private String offlineApplicationsOnlyFlag;
    private String vacancyDuration;
    private String vacancyEndDate;
    private String dateAdded;
    private String ApplicationFormId;
    private String RecruitmentProgramId;
    private String CloseDateClause;
    private boolean isSaved;
    private String RecruitmentProgramUrl;
    private String closingDateAsDateTime;
    private int lowestRemuneration;
    private int highestRemuneration;
    private String headingLabelText;
    private boolean canApplyOnline;
    private boolean hasSpecialInstructions;
    private String endDateDurationDetail;
    private String endDateDurationLabel;
    private boolean isTempOrCasual;
    private String formattedClosingDate;
    private boolean hasRecruitmentProgramUrl;
    private String getFormattedDesignationList;
    private JsonArray vacancyDesignationList;
    private JsonArray attachmentsList;
    private static HashMap<Integer, NT> hashes = new HashMap<>();
    public int getRtfId() {
        return rtfId;
    }
    public void setRtfId(int rtfId) {
        this.rtfId = rtfId;
    }
    public String getPositionNumber() {
        return positionNumber;
    }
    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }
    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getVacancyType() {
        return vacancyType;
    }
    public void setVacancyType(String vacancyType) {
        this.vacancyType = vacancyType;
    }
    public String getAltVacancyType() {
        return altVacancyType;
    }
    public void setAltVacancyType(String altVacancyType) {
        this.altVacancyType = altVacancyType;
    }
    public String getNumberOfVacancies() {
        return numberOfVacancies;
    }
    public void setNumberOfVacancies(String numberOfVacancies) {
        this.numberOfVacancies = numberOfVacancies;
    }
    public String getVacancyTypeCodeValue() {
        return vacancyTypeCodeValue;
    }
    public void setVacancyTypeCodeValue(String vacancyTypeCodeValue) {
        this.vacancyTypeCodeValue = vacancyTypeCodeValue;
    }
    public String getAgency() {
        return agency;
    }
    public void setAgency(String agency) {
        this.agency = agency;
    }
    public String getSection() {
        return section;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public String getContactPerson() {
        return contactPerson;
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    public String getPrimaryObjective() {
        return primaryObjective;
    }
    public void setPrimaryObjective(String primaryObjective) {
        this.primaryObjective = primaryObjective;
    }
    public String getAdvertisingType() {
        return advertisingType;
    }
    public void setAdvertisingType(String advertisingType) {
        this.advertisingType = advertisingType;
    }
    public String getApplicationType() {
        return applicationType;
    }
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    public String getClosingDate() {
        return closingDate;
    }
    public void setClosingDate(String closingDate) {
        this.closingDate = closingDate;
    }
    public String getLocations() {
        return locations;
    }
    public void setLocations(String locations) {
        this.locations = locations;
    }
    public String getDesignations() {
        return designations;
    }
    public void setDesignations(String designations) {
        this.designations = designations;
    }
    public boolean isCanceled() {
        return isCanceled;
    }
    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
    public String getOfflineApplicationsOnlyFlag() {
        return offlineApplicationsOnlyFlag;
    }
    public void setOfflineApplicationsOnlyFlag(String offlineApplicationsOnlyFlag) {
        this.offlineApplicationsOnlyFlag = offlineApplicationsOnlyFlag;
    }
    public String getVacancyDuration() {
        return vacancyDuration;
    }
    public void setVacancyDuration(String vacancyDuration) {
        this.vacancyDuration = vacancyDuration;
    }
    public String getVacancyEndDate() {
        return vacancyEndDate;
    }
    public void setVacancyEndDate(String vacancyEndDate) {
        this.vacancyEndDate = vacancyEndDate;
    }
    public String getDateAdded() {
        return dateAdded;
    }
    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
    public String getApplicationFormId() {
        return ApplicationFormId;
    }
    public void setApplicationFormId(String applicationFormId) {
        ApplicationFormId = applicationFormId;
    }
    public String getRecruitmentProgramId() {
        return RecruitmentProgramId;
    }
    public void setRecruitmentProgramId(String recruitmentProgramId) {
        RecruitmentProgramId = recruitmentProgramId;
    }
    public String getCloseDateClause() {
        return CloseDateClause;
    }
    public void setCloseDateClause(String closeDateClause) {
        CloseDateClause = closeDateClause;
    }
    public boolean isSaved() {
        return isSaved;
    }
    public void setSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }
    public String getRecruitmentProgramUrl() {
        return RecruitmentProgramUrl;
    }
    public void setRecruitmentProgramUrl(String recruitmentProgramUrl) {
        RecruitmentProgramUrl = recruitmentProgramUrl;
    }
    public String getClosingDateAsDateTime() {
        return closingDateAsDateTime;
    }
    public void setClosingDateAsDateTime(String closingDateAsDateTime) {
        this.closingDateAsDateTime = closingDateAsDateTime;
    }
    public int getLowestRemuneration() {
        return lowestRemuneration;
    }
    public void setLowestRemuneration(int lowestRemuneration) {
        this.lowestRemuneration = lowestRemuneration;
    }
    public int getHighestRemuneration() {
        return highestRemuneration;
    }
    public void setHighestRemuneration(int highestRemuneration) {
        this.highestRemuneration = highestRemuneration;
    }
    public String getHeadingLabelText() {
        return headingLabelText;
    }
    public void setHeadingLabelText(String headingLabelText) {
        this.headingLabelText = headingLabelText;
    }
    public boolean isCanApplyOnline() {
        return canApplyOnline;
    }
    public void setCanApplyOnline(boolean canApplyOnline) {
        this.canApplyOnline = canApplyOnline;
    }
    public boolean isHasSpecialInstructions() {
        return hasSpecialInstructions;
    }
    public void setHasSpecialInstructions(boolean hasSpecialInstructions) {
        this.hasSpecialInstructions = hasSpecialInstructions;
    }
    public String getEndDateDurationDetail() {
        return endDateDurationDetail;
    }
    public void setEndDateDurationDetail(String endDateDurationDetail) {
        this.endDateDurationDetail = endDateDurationDetail;
    }
    public String getEndDateDurationLabel() {
        return endDateDurationLabel;
    }
    public void setEndDateDurationLabel(String endDateDurationLabel) {
        this.endDateDurationLabel = endDateDurationLabel;
    }
    public boolean isTempOrCasual() {
        return isTempOrCasual;
    }
    public void setTempOrCasual(boolean isTempOrCasual) {
        this.isTempOrCasual = isTempOrCasual;
    }
    public String getFormattedClosingDate() {
        return formattedClosingDate;
    }
    public void setFormattedClosingDate(String formattedClosingDate) {
        this.formattedClosingDate = formattedClosingDate;
    }
    public boolean isHasRecruitmentProgramUrl() {
        return hasRecruitmentProgramUrl;
    }
    public void setHasRecruitmentProgramUrl(boolean hasRecruitmentProgramUrl) {
        this.hasRecruitmentProgramUrl = hasRecruitmentProgramUrl;
    }
    public String getGetFormattedDesignationList() {
        return getFormattedDesignationList;
    }
    public void setGetFormattedDesignationList(String getFormattedDesignationList) {
        this.getFormattedDesignationList = getFormattedDesignationList;
    }
    public JsonArray getVacancyDesignationList() {
        return vacancyDesignationList;
    }
    public void setVacancyDesignationList(JsonArray vacancyDesignationList) {
        this.vacancyDesignationList = vacancyDesignationList;
    }
    public JsonArray getAttachmentsList() {
        return attachmentsList;
    }
    public void setAttachmentsList(JsonArray attachmentsList) {
        this.attachmentsList = attachmentsList;
    }
    @Override
    public String[] toData() {
        System.out.println(RecruitmentProgramUrl + " - NT link");
        return new String[] {jobTitle, locations, agency, RecruitmentProgramUrl, String.valueOf(rtfId), closingDate, lowestRemuneration + " - " + highestRemuneration};
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'toData'");
    }
    @Override
    public boolean putinHash() {
        int hashcode = hashCode();
        NT exist = hashes.get(hashcode);
        if (exist == null) {
            hashes.put(hashcode, this);
            return false;
        } else {
            return true;
        }
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'putinHash'");
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + rtfId;
        result = prime * result + ((positionNumber == null) ? 0 : positionNumber.hashCode());
        result = prime * result + ((jobTitle == null) ? 0 : jobTitle.hashCode());
        result = prime * result + ((vacancyType == null) ? 0 : vacancyType.hashCode());
        result = prime * result + ((altVacancyType == null) ? 0 : altVacancyType.hashCode());
        result = prime * result + ((numberOfVacancies == null) ? 0 : numberOfVacancies.hashCode());
        result = prime * result + ((vacancyTypeCodeValue == null) ? 0 : vacancyTypeCodeValue.hashCode());
        result = prime * result + ((agency == null) ? 0 : agency.hashCode());
        result = prime * result + ((section == null) ? 0 : section.hashCode());
        result = prime * result + ((contactPerson == null) ? 0 : contactPerson.hashCode());
        result = prime * result + ((primaryObjective == null) ? 0 : primaryObjective.hashCode());
        result = prime * result + ((advertisingType == null) ? 0 : advertisingType.hashCode());
        result = prime * result + ((applicationType == null) ? 0 : applicationType.hashCode());
        result = prime * result + ((specialInstructions == null) ? 0 : specialInstructions.hashCode());
        result = prime * result + ((closingDate == null) ? 0 : closingDate.hashCode());
        result = prime * result + ((locations == null) ? 0 : locations.hashCode());
        result = prime * result + ((designations == null) ? 0 : designations.hashCode());
        result = prime * result + (isCanceled ? 1231 : 1237);
        result = prime * result + ((offlineApplicationsOnlyFlag == null) ? 0 : offlineApplicationsOnlyFlag.hashCode());
        result = prime * result + ((vacancyDuration == null) ? 0 : vacancyDuration.hashCode());
        result = prime * result + ((vacancyEndDate == null) ? 0 : vacancyEndDate.hashCode());
        result = prime * result + ((dateAdded == null) ? 0 : dateAdded.hashCode());
        result = prime * result + ((ApplicationFormId == null) ? 0 : ApplicationFormId.hashCode());
        result = prime * result + ((RecruitmentProgramId == null) ? 0 : RecruitmentProgramId.hashCode());
        result = prime * result + ((CloseDateClause == null) ? 0 : CloseDateClause.hashCode());
        result = prime * result + (isSaved ? 1231 : 1237);
        result = prime * result + ((RecruitmentProgramUrl == null) ? 0 : RecruitmentProgramUrl.hashCode());
        result = prime * result + ((closingDateAsDateTime == null) ? 0 : closingDateAsDateTime.hashCode());
        result = prime * result + lowestRemuneration;
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
        NT other = (NT) obj;
        if (rtfId != other.rtfId)
            return false;
        if (positionNumber == null) {
            if (other.positionNumber != null)
                return false;
        } else if (!positionNumber.equals(other.positionNumber))
            return false;
        if (jobTitle == null) {
            if (other.jobTitle != null)
                return false;
        } else if (!jobTitle.equals(other.jobTitle))
            return false;
        if (vacancyType == null) {
            if (other.vacancyType != null)
                return false;
        } else if (!vacancyType.equals(other.vacancyType))
            return false;
        if (altVacancyType == null) {
            if (other.altVacancyType != null)
                return false;
        } else if (!altVacancyType.equals(other.altVacancyType))
            return false;
        if (numberOfVacancies == null) {
            if (other.numberOfVacancies != null)
                return false;
        } else if (!numberOfVacancies.equals(other.numberOfVacancies))
            return false;
        if (vacancyTypeCodeValue == null) {
            if (other.vacancyTypeCodeValue != null)
                return false;
        } else if (!vacancyTypeCodeValue.equals(other.vacancyTypeCodeValue))
            return false;
        if (agency == null) {
            if (other.agency != null)
                return false;
        } else if (!agency.equals(other.agency))
            return false;
        if (section == null) {
            if (other.section != null)
                return false;
        } else if (!section.equals(other.section))
            return false;
        if (contactPerson == null) {
            if (other.contactPerson != null)
                return false;
        } else if (!contactPerson.equals(other.contactPerson))
            return false;
        if (primaryObjective == null) {
            if (other.primaryObjective != null)
                return false;
        } else if (!primaryObjective.equals(other.primaryObjective))
            return false;
        if (advertisingType == null) {
            if (other.advertisingType != null)
                return false;
        } else if (!advertisingType.equals(other.advertisingType))
            return false;
        if (applicationType == null) {
            if (other.applicationType != null)
                return false;
        } else if (!applicationType.equals(other.applicationType))
            return false;
        if (specialInstructions == null) {
            if (other.specialInstructions != null)
                return false;
        } else if (!specialInstructions.equals(other.specialInstructions))
            return false;
        if (closingDate == null) {
            if (other.closingDate != null)
                return false;
        } else if (!closingDate.equals(other.closingDate))
            return false;
        if (locations == null) {
            if (other.locations != null)
                return false;
        } else if (!locations.equals(other.locations))
            return false;
        if (designations == null) {
            if (other.designations != null)
                return false;
        } else if (!designations.equals(other.designations))
            return false;
        if (isCanceled != other.isCanceled)
            return false;
        if (offlineApplicationsOnlyFlag == null) {
            if (other.offlineApplicationsOnlyFlag != null)
                return false;
        } else if (!offlineApplicationsOnlyFlag.equals(other.offlineApplicationsOnlyFlag))
            return false;
        if (vacancyDuration == null) {
            if (other.vacancyDuration != null)
                return false;
        } else if (!vacancyDuration.equals(other.vacancyDuration))
            return false;
        if (vacancyEndDate == null) {
            if (other.vacancyEndDate != null)
                return false;
        } else if (!vacancyEndDate.equals(other.vacancyEndDate))
            return false;
        if (dateAdded == null) {
            if (other.dateAdded != null)
                return false;
        } else if (!dateAdded.equals(other.dateAdded))
            return false;
        if (ApplicationFormId == null) {
            if (other.ApplicationFormId != null)
                return false;
        } else if (!ApplicationFormId.equals(other.ApplicationFormId))
            return false;
        if (RecruitmentProgramId == null) {
            if (other.RecruitmentProgramId != null)
                return false;
        } else if (!RecruitmentProgramId.equals(other.RecruitmentProgramId))
            return false;
        if (CloseDateClause == null) {
            if (other.CloseDateClause != null)
                return false;
        } else if (!CloseDateClause.equals(other.CloseDateClause))
            return false;
        if (isSaved != other.isSaved)
            return false;
        if (RecruitmentProgramUrl == null) {
            if (other.RecruitmentProgramUrl != null)
                return false;
        } else if (!RecruitmentProgramUrl.equals(other.RecruitmentProgramUrl))
            return false;
        if (closingDateAsDateTime == null) {
            if (other.closingDateAsDateTime != null)
                return false;
        } else if (!closingDateAsDateTime.equals(other.closingDateAsDateTime))
            return false;
        if (lowestRemuneration != other.lowestRemuneration)
            return false;
        return true;
    }

}
