package com.azarpark.watchman.models;

import java.util.List;

public class TicketMessage {
    private String name;
    private List<TicketMessagePart> prefix;
    private List<TicketMessagePart> postfix;
    private String note;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TicketMessagePart> getPrefix() {
        return prefix;
    }

    public void setPrefix(List<TicketMessagePart> prefix) {
        this.prefix = prefix;
    }

    public List<TicketMessagePart> getPostfix() {
        return postfix;
    }

    public void setPostfix(List<TicketMessagePart> postfix) {
        this.postfix = postfix;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "TicketMessage{" +
                "name='" + name + '\'' +
                ", prefix=" + prefix +
                ", postfix=" + postfix +
                ", note='" + note + '\'' +
                '}';
    }
}
