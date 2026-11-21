package model;

/** Exposes the canonical project status codes to JSP EL. */
public class ProjectStatusCodes {
    public String getPending() { return Project.STATUS_PENDING; }
    public String getApproved() { return Project.STATUS_APPROVED; }
    public String getInProgress() { return Project.STATUS_IN_PROGRESS; }
    public String getCompleted() { return Project.STATUS_COMPLETED; }
    public String getCanceled() { return Project.STATUS_CANCELED; }
    public String getRejected() { return Project.STATUS_REJECTED; }
}
