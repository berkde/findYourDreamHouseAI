package com.dreamhouse.ai.house.model.request;

import java.time.OffsetDateTime;
import java.util.Objects;

public class HouseAdMessageSendRequestModel {
    private String subject;
    private String message;
    private OffsetDateTime messageDate;
    private String senderEmail;
    private String senderName;
    private String senderPhone;
    private String receiverEmail;
    private String receiverHouseAdUid;

    public HouseAdMessageSendRequestModel() {
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

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverHouseAdUid() {
        return receiverHouseAdUid;
    }

    public void setReceiverHouseAdUid(String receiverHouseAdUid) {
        this.receiverHouseAdUid = receiverHouseAdUid;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HouseAdMessageSendRequestModel that)) return false;
        return Objects.equals(senderEmail, that.senderEmail) && Objects.equals(receiverHouseAdUid, that.receiverHouseAdUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderEmail, receiverHouseAdUid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HouseAdMessageSendRequestModel{");
        sb.append("subject='").append(subject).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", messageDate=").append(messageDate);
        sb.append(", senderEmail='").append(senderEmail).append('\'');
        sb.append(", senderName='").append(senderName).append('\'');
        sb.append(", senderPhone='").append(senderPhone).append('\'');
        sb.append(", receiverEmail='").append(receiverEmail).append('\'');
        sb.append(", receiverHouseAdUid='").append(receiverHouseAdUid).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
