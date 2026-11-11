package com.collegefest.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "judge")
public class Judge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Email(message = "Enter a valid email")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String expertise;

    @Column(unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", length = 64)
    private String passwordHash;

    @JsonIgnore
    @OneToMany(mappedBy = "judge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scores = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "judge_event_assignment",
        joinColumns = @JoinColumn(name = "judge_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> assignedEvents = new ArrayList<>();

    public Judge() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public List<Event> getAssignedEvents() {
        return assignedEvents;
    }

    public void setAssignedEvents(List<Event> assignedEvents) {
        this.assignedEvents = assignedEvents;
    }
}
