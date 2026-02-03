package com.wclw.argueseye.dto;

import java.util.ArrayList;
import java.util.List;

public class RiskResult {
    private int score;
    private List<String> messages = new ArrayList<>();


    public RiskResult(int socre,List<String> messages){
        this.score = socre;
        if(messages.isEmpty()){messages.add("");}
        this.messages = messages;
    }

    public int getScore() {
        return score;
    }

    public List<String> getMessages() {
        return messages;
    }
}
