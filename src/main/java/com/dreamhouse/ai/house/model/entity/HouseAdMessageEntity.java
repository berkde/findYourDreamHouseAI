package com.dreamhouse.ai.house.model.entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "house_ad_messages",
        indexes = @Index(name = "idx_house_ad_msg_fk", columnList = "house_ad_fk")
)
public class HouseAdMessageEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_uid", unique = true, nullable = false, length = 64)
    private String messageUid;

    @Column(length = 256)
    private String subject;

    @Column(columnDefinition = "text")
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "message_date", nullable = false)
    private OffsetDateTime messageDate;

    @Column(name = "sender_email", nullable = false, length = 320)
    private String senderEmail;

    @Column(name = "sender_name", nullable = false, length = 255)
    private String senderName;

    @Column(name = "sender_phone", nullable = false, length = 32)
    private String senderPhone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_ad_fk", nullable = false)
    private HouseAdEntity houseAd;

    public HouseAdMessageEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public HouseAdEntity getHouseAd() {
        return houseAd;
    }

    public void setHouseAd(HouseAdEntity houseAd) {
        this.houseAd = houseAd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HouseAdMessageEntity that)) return false;
        return Objects.equals(messageUid, that.messageUid);
    }

    @Override
    public int hashCode() { return Objects.hash(messageUid); }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HouseAdMessage{");
        sb.append("messageUid='").append(messageUid).append('\'');
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", messageDate='").append(messageDate).append('\'');
        sb.append(", senderEmail='").append(senderEmail).append('\'');
        sb.append(", senderName='").append(senderName).append('\'');
        sb.append(", senderPhone='").append(senderPhone).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
