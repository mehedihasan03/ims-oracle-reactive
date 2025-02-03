package net.celloscope.mraims.loanportfolio.core.util.enums;

public enum LoanTypeID {

    LOAN_TYPE_M("M"),
    LOAN_TYPE_SS("SS");

    private final String value;

    LoanTypeID(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
