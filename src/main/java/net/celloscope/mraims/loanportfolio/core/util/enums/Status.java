package net.celloscope.mraims.loanportfolio.core.util.enums;

public enum Status {
    STATUS_ACTIVE("Active"),
    STATUS_INACTIVE("Inactive"),
    STATUS_APPROVED("Approved"),
    STATUS_WAITING("Waiting"),
    STATUS_PROCESSING("Processing"),
    STATUS_PENDING("Pending"),
    STATUS_FINISHED("Finished"),
    STATUS_STAGED("Staged"),
    STATUS_COMPLETED("Completed"),
    STATUS_INCOMPLETE("Incomplete"),
    STATUS_PAID("Paid"),
    STATUS_REJECTED("Rejected"),
    STATUS_CANCELLED("Canceled"),
    STATUS_RESCHEDULED("Rescheduled"),
    STATUS_FAILED("Failed"),
    STATUS_EXCEPTION("Exception"),
    STATUS_SUBMITTED("Submitted"),
    STATUS_UNAUTHORIZED("Unauthorized"),
    STATUS_REGENERATED("Regenerated"),
    STATUS_INVALIDATED("Invalidated"),
    STATUS_MATURED("Matured"),
    STATUS_PENDING_APPROVAL("Pending-Approval"),
    STATUS_CLOSED("Closed"),
    STATUS_CLOSED_PAID_OFF("Closed-Paid-Off"),
    STATUS_PAID_OFF("Paid-Off"),
    STATUS_OPENED("Opened"),
    STATUS_YES("Yes"),
    STATUS_NO("No"),
    STATUS_ARCHIVED("Archived"),
    STATUS_RETRYING("Retrying"),
    STATUS_REBATED("Rebated"),
    STATUS_REVISED("Revised"),
    STATUS_CLOSED_WRITTEN_OFF("Closed-Written-Off");


    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
