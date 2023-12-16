package com.azarpark.watchman.models;

import android.content.Context;
import android.view.ViewGroup;

import com.azarpark.watchman.utils.Assistant;

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

    public boolean inflateNote(Context context, ViewGroup parent, boolean attachToParent){
        return Assistant.inflateHTML(null, note, context, parent, attachToParent);
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
