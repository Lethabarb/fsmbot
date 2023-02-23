package FSM.entities.jobs;

import java.util.HashMap;

import FSM.services.jobs.ToDataService;

public class APS implements ToDataService {
    private String agencyEmploymentAct;
    private String agencyInformationURL;
    private String applicationURL;
    private String departmentDescription;
    private String departmentName;
    private String departmentURL;
    private String jobCategory;
    private String jobClassification;
    private String jobCloseDate;
    private String jobContact;
    private String jobContactPhone;
    private String jobDuties;
    private String jobId;
    private String jobLocation;
    private String jobName;
    private String jobPostedDate;
    private int jobSalaryFrom;
    private int jobSalaryTo;
    private String jobStatus;
    private String jobType;
    private String positionNumberString;
    private String recordType;
    private String vacancyNumber;

    private static HashMap<Integer, APS> hashes = new HashMap<>();

    public APS(String agencyEmploymentAct, String agencyInformationURL, String applicationURL,
            String departmentDescription, String departmentName, String departmentURL, String jobCategory,
            String jobClassification, String jobCloseDate, String jobContact, String jobContactPhone, String jobDuties,
            String jobId, String jobLocation, String jobName, String jobPostedDate, int jobSalaryFrom, int jobSalaryTo,
            String jobStatus, String jobType, String positionNumberString, String recordType, String vacancyNumber) {
        this.agencyEmploymentAct = agencyEmploymentAct;
        this.agencyInformationURL = agencyInformationURL;
        this.applicationURL = applicationURL;
        this.departmentDescription = departmentDescription;
        this.departmentName = departmentName;
        this.departmentURL = departmentURL;
        this.jobCategory = jobCategory;
        this.jobClassification = jobClassification;
        this.jobCloseDate = jobCloseDate;
        this.jobContact = jobContact;
        this.jobContactPhone = jobContactPhone;
        this.jobDuties = jobDuties;
        this.jobId = jobId;
        this.jobLocation = jobLocation;
        this.jobName = jobName;
        this.jobPostedDate = jobPostedDate;
        this.jobSalaryFrom = jobSalaryFrom;
        this.jobSalaryTo = jobSalaryTo;
        this.jobStatus = jobStatus;
        this.jobType = jobType;
        this.positionNumberString = positionNumberString;
        this.recordType = recordType;
        this.vacancyNumber = vacancyNumber;
    }

    public String getAgencyEmploymentAct() {
        return agencyEmploymentAct;
    }

    public void setAgencyEmploymentAct(String agencyEmploymentAct) {
        this.agencyEmploymentAct = agencyEmploymentAct;
    }

    public String getAgencyInformationURL() {
        return agencyInformationURL;
    }

    public void setAgencyInformationURL(String agencyInformationURL) {
        this.agencyInformationURL = agencyInformationURL;
    }

    public String getApplicationURL() {
        return applicationURL;
    }

    public void setApplicationURL(String applicationURL) {
        this.applicationURL = applicationURL;
    }

    public String getDepartmentDescription() {
        return departmentDescription;
    }

    public void setDepartmentDescription(String departmentDescription) {
        this.departmentDescription = departmentDescription;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentURL() {
        return departmentURL;
    }

    public void setDepartmentURL(String departmentURL) {
        this.departmentURL = departmentURL;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getJobClassification() {
        return jobClassification;
    }

    public void setJobClassification(String jobClassification) {
        this.jobClassification = jobClassification;
    }

    public String getJobCloseDate() {
        return jobCloseDate;
    }

    public void setJobCloseDate(String jobCloseDate) {
        this.jobCloseDate = jobCloseDate;
    }

    public String getJobContact() {
        return jobContact;
    }

    public void setJobContact(String jobContact) {
        this.jobContact = jobContact;
    }

    public String getJobContactPhone() {
        return jobContactPhone;
    }

    public void setJobContactPhone(String jobContactPhone) {
        this.jobContactPhone = jobContactPhone;
    }

    public String getJobDuties() {
        return jobDuties;
    }

    public void setJobDuties(String jobDuties) {
        this.jobDuties = jobDuties;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobPostedDate() {
        return jobPostedDate;
    }

    public void setJobPostedDate(String jobPostedDate) {
        this.jobPostedDate = jobPostedDate;
    }

    public int getJobSalaryFrom() {
        return jobSalaryFrom;
    }

    public void setJobSalaryFrom(int jobSalaryFrom) {
        this.jobSalaryFrom = jobSalaryFrom;
    }

    public int getJobSalaryTo() {
        return jobSalaryTo;
    }

    public void setJobSalaryTo(int jobSalaryTo) {
        this.jobSalaryTo = jobSalaryTo;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getPositionNumberString() {
        return positionNumberString;
    }

    public void setPositionNumberString(String positionNumberString) {
        this.positionNumberString = positionNumberString;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getVacancyNumber() {
        return vacancyNumber;
    }

    public void setVacancyNumber(String vacancyNumber) {
        this.vacancyNumber = vacancyNumber;
    }

    public String[] toData() {
        return new String[] {jobName, jobLocation, departmentName, applicationURL, vacancyNumber, jobCloseDate, jobSalaryFrom + "-" + jobSalaryTo};
        // TODO Auto-generated method stub
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agencyEmploymentAct == null) ? 0 : agencyEmploymentAct.hashCode());
        result = prime * result + ((agencyInformationURL == null) ? 0 : agencyInformationURL.hashCode());
        result = prime * result + ((applicationURL == null) ? 0 : applicationURL.hashCode());
        result = prime * result + ((departmentDescription == null) ? 0 : departmentDescription.hashCode());
        result = prime * result + ((departmentName == null) ? 0 : departmentName.hashCode());
        result = prime * result + ((departmentURL == null) ? 0 : departmentURL.hashCode());
        result = prime * result + ((jobCategory == null) ? 0 : jobCategory.hashCode());
        result = prime * result + ((jobClassification == null) ? 0 : jobClassification.hashCode());
        result = prime * result + ((jobCloseDate == null) ? 0 : jobCloseDate.hashCode());
        result = prime * result + ((jobContact == null) ? 0 : jobContact.hashCode());
        result = prime * result + ((jobContactPhone == null) ? 0 : jobContactPhone.hashCode());
        result = prime * result + ((jobDuties == null) ? 0 : jobDuties.hashCode());
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((jobLocation == null) ? 0 : jobLocation.hashCode());
        result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
        result = prime * result + ((jobPostedDate == null) ? 0 : jobPostedDate.hashCode());
        result = prime * result + jobSalaryFrom;
        result = prime * result + jobSalaryTo;
        result = prime * result + ((jobStatus == null) ? 0 : jobStatus.hashCode());
        result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
        result = prime * result + ((positionNumberString == null) ? 0 : positionNumberString.hashCode());
        result = prime * result + ((recordType == null) ? 0 : recordType.hashCode());
        result = prime * result + ((vacancyNumber == null) ? 0 : vacancyNumber.hashCode());
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
        APS other = (APS) obj;
        if (agencyEmploymentAct == null) {
            if (other.agencyEmploymentAct != null)
                return false;
        } else if (!agencyEmploymentAct.equals(other.agencyEmploymentAct))
            return false;
        if (agencyInformationURL == null) {
            if (other.agencyInformationURL != null)
                return false;
        } else if (!agencyInformationURL.equals(other.agencyInformationURL))
            return false;
        if (applicationURL == null) {
            if (other.applicationURL != null)
                return false;
        } else if (!applicationURL.equals(other.applicationURL))
            return false;
        if (departmentDescription == null) {
            if (other.departmentDescription != null)
                return false;
        } else if (!departmentDescription.equals(other.departmentDescription))
            return false;
        if (departmentName == null) {
            if (other.departmentName != null)
                return false;
        } else if (!departmentName.equals(other.departmentName))
            return false;
        if (departmentURL == null) {
            if (other.departmentURL != null)
                return false;
        } else if (!departmentURL.equals(other.departmentURL))
            return false;
        if (jobCategory == null) {
            if (other.jobCategory != null)
                return false;
        } else if (!jobCategory.equals(other.jobCategory))
            return false;
        if (jobClassification == null) {
            if (other.jobClassification != null)
                return false;
        } else if (!jobClassification.equals(other.jobClassification))
            return false;
        if (jobCloseDate == null) {
            if (other.jobCloseDate != null)
                return false;
        } else if (!jobCloseDate.equals(other.jobCloseDate))
            return false;
        if (jobContact == null) {
            if (other.jobContact != null)
                return false;
        } else if (!jobContact.equals(other.jobContact))
            return false;
        if (jobContactPhone == null) {
            if (other.jobContactPhone != null)
                return false;
        } else if (!jobContactPhone.equals(other.jobContactPhone))
            return false;
        if (jobDuties == null) {
            if (other.jobDuties != null)
                return false;
        } else if (!jobDuties.equals(other.jobDuties))
            return false;
        if (jobId == null) {
            if (other.jobId != null)
                return false;
        } else if (!jobId.equals(other.jobId))
            return false;
        if (jobLocation == null) {
            if (other.jobLocation != null)
                return false;
        } else if (!jobLocation.equals(other.jobLocation))
            return false;
        if (jobName == null) {
            if (other.jobName != null)
                return false;
        } else if (!jobName.equals(other.jobName))
            return false;
        if (jobPostedDate == null) {
            if (other.jobPostedDate != null)
                return false;
        } else if (!jobPostedDate.equals(other.jobPostedDate))
            return false;
        if (jobSalaryFrom != other.jobSalaryFrom)
            return false;
        if (jobSalaryTo != other.jobSalaryTo)
            return false;
        if (jobStatus == null) {
            if (other.jobStatus != null)
                return false;
        } else if (!jobStatus.equals(other.jobStatus))
            return false;
        if (jobType == null) {
            if (other.jobType != null)
                return false;
        } else if (!jobType.equals(other.jobType))
            return false;
        if (positionNumberString == null) {
            if (other.positionNumberString != null)
                return false;
        } else if (!positionNumberString.equals(other.positionNumberString))
            return false;
        if (recordType == null) {
            if (other.recordType != null)
                return false;
        } else if (!recordType.equals(other.recordType))
            return false;
        if (vacancyNumber == null) {
            if (other.vacancyNumber != null)
                return false;
        } else if (!vacancyNumber.equals(other.vacancyNumber))
            return false;
        return true;
    }

    public boolean putinHash() {
        int hashcode = hashCode();
        APS exist = hashes.get(hashcode);
        if (exist == null) {
            hashes.put(hashcode, this);
            return false;
        } else {
            return true;
        }
    }
}
