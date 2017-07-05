package com.farhanapps.HitChat.Models;

/**
 * Created by farhan on 23-04-2016.
 */
public class ContactModel {
    int contactId;
    String contact_name,contact_number,contact_pic_thumb,contact_pic,contact_cover,contact_cover_thumb,contact_status;

    public ContactModel() {
    }

    public ContactModel(int contactId,String contact_name, String contact_number, String contact_pic_thumb, String contact_pic, String contact_cover, String contact_cover_thumb, String contact_status) {
        this.contactId=contactId;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
        this.contact_pic_thumb = contact_pic_thumb;
        this.contact_pic = contact_pic;
        this.contact_cover = contact_cover;
        this.contact_cover_thumb = contact_cover_thumb;
        this.contact_status = contact_status;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public void setContact_pic_thumb(String contact_pic_thumb) {
        this.contact_pic_thumb = contact_pic_thumb;
    }

    public void setContact_pic(String contact_pic) {
        this.contact_pic = contact_pic;
    }

    public void setContact_cover(String contact_cover) {
        this.contact_cover = contact_cover;
    }

    public void setContact_cover_thumb(String contact_cover_thumb) {
        this.contact_cover_thumb = contact_cover_thumb;
    }

    public void setContact_status(String contact_status) {
        this.contact_status = contact_status;
    }

    public String getContact_name() {
        return contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public String getContact_pic_thumb() {
        return contact_pic_thumb;
    }

    public String getContact_pic() {
        return contact_pic;
    }

    public String getContact_cover() {
        return contact_cover;
    }

    public String getContact_cover_thumb() {
        return contact_cover_thumb;
    }

    public String getContact_status() {
        return contact_status;
    }
}
