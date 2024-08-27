package com.rakuten.hackathon.data.feed.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cusers")
public class User {

    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "username")
    private String username;

    @Column(name = "user_email")
    private String useremail;

    @Column(name = "hashed_password")
    private String hashedPassword;
    
    @Column(name = "doc_visit_days")
    private int docVisitDays;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Id
    @Column(name = "user_id")
    private String userid;
    
    @Column(name = "user_mask")
    private int userMask;
    
    @Column(name = "user_group")
    private String userGroup;
    
    @Column(name = "user_status")
    private String userStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "hl_llm_visit_days")
    private int hlLlmVisitDays;
    

    public User() {

    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getUseremail() {
        return useremail;
    }


    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }


    public String getHashedPassword() {
        return hashedPassword;
    }


    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }


    public int getDocVisitDays() {
        return docVisitDays;
    }


    public void setDocVisitDays(int docVisitDays) {
        this.docVisitDays = docVisitDays;
    }


    public LocalDateTime getLastLogin() {
        return lastLogin;
    }


    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }


    public String getUserid() {
        return userid;
    }


    public void setUserid(String userid) {
        this.userid = userid;
    }


    public int getUserMask() {
        return userMask;
    }


    public void setUserMask(int userMask) {
        this.userMask = userMask;
    }


    public String getUserGroup() {
        return userGroup;
    }


    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }


    public String getUserStatus() {
        return userStatus;
    }


    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public int getHlLlmVisitDays() {
        return hlLlmVisitDays;
    }


    public void setHlLlmVisitDays(int hlLlmVisitDays) {
        this.hlLlmVisitDays = hlLlmVisitDays;
    }
}
