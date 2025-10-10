package com.dreamhouse.ai.house.dto;


import java.time.OffsetDateTime;
import java.util.Objects;

public class HouseAdMessageDTO {
    private String messageUid;
    private String subject;
    private String message;
    private OffsetDateTime messageDate;
    private String senderEmail;
    private String senderName;
    private String senderPhone;
    private String houseAdUid;

    public HouseAdMessageDTO() {
    }

    public String getMessageUid() {
        return messageUid;
    }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(OffsetDateTime messageDate) {
        this.messageDate = messageDate;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getHouseAdUid() {
        return houseAdUid;
    }

    public void setHouseAdUid(HouseAdDTO houseAd) {
        this.houseAdUid = houseAdUid;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HouseAdMessageDTO that)) return false;
        return Objects.equals(messageUid, that.messageUid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageUid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HouseAdMessageDTO{");
        sb.append("messageUid='").append(messageUid).append('\'');
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", messageDate=").append(messageDate);
        sb.append(", senderEmail='").append(senderEmail).append('\'');
        sb.append(", senderName='").append(senderName).append('\'');
        sb.append(", senderPhone='").append(senderPhone).append('\'');
        sb.append(", houseAdUid=").append(houseAdUid);
        sb.append('}');
        return sb.toString();
    }
}
