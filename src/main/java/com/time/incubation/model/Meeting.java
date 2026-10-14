package com.time.incubation.model;

import java.time.LocalDate;

public class Meeting {

    private String meetingId;
    private String startupId;
    private String mentorId;
    private String managerId;
    private LocalDate meetingDate;
    private String meetingTime;
    private MeetingType meetingType;
    private String discussionNotes;
    private String nextAction;
    private LocalDate nextMeetingDate;

    public Meeting(String meetingId, String startupId, String mentorId, String managerId,
                   LocalDate meetingDate, String meetingTime, MeetingType meetingType,
                   String discussionNotes, String nextAction, LocalDate nextMeetingDate) {
        this.meetingId = meetingId;
        this.startupId = startupId;
        this.mentorId = mentorId;
        this.managerId = managerId;
        this.meetingDate = meetingDate;
        this.meetingTime = meetingTime;
        this.meetingType = meetingType;
        this.discussionNotes = discussionNotes;
        this.nextAction = nextAction;
        this.nextMeetingDate = nextMeetingDate;
    }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public LocalDate getMeetingDate() { return meetingDate; }
    public void setMeetingDate(LocalDate meetingDate) { this.meetingDate = meetingDate; }

    public String getMeetingTime() { return meetingTime; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }

    public MeetingType getMeetingType() { return meetingType; }
    public void setMeetingType(MeetingType meetingType) { this.meetingType = meetingType; }

    public String getDiscussionNotes() { return discussionNotes; }
    public void setDiscussionNotes(String discussionNotes) { this.discussionNotes = discussionNotes; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public LocalDate getNextMeetingDate() { return nextMeetingDate; }
    public void setNextMeetingDate(LocalDate nextMeetingDate) { this.nextMeetingDate = nextMeetingDate; }

    @Override
    public String toString() {
        return meetingId + " | " + meetingType + " | " + meetingDate;
    }
}