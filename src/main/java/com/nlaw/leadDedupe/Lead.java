package com.nlaw.leadDedupe;

/**
 *  Lead JavaBean
 *
 *  Leads in this context are contact records used in sales/marketing.
 *
 *  @author nlawrence
 *
 */
public class Lead {

    private String _id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String entryDate;

    public Lead(String _id, String email, String firstName, String lastName,
                String address, String entryDate) {
        this._id = _id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.entryDate = entryDate;
    }

    @Override
    public String toString (){
        return "{\n   id: " + get_id() + ",\n   email: " + getEmail() +
                ",\n   firstName: " + getFirstName() + ",\n   lastName: " +
                getLastName() + ",\n   address: " + getAddress() +
                ",\n   entryDate: " + getEntryDate() + "\n  }";
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
}
