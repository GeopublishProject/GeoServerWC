package com.geopublish.geoserver.model;

import java.util.List;

/**
 * Created by edgar on 02/09/2015.
 */
public class UpdatePositionResult {

    private String result;
    private List<UserMessage> userMessages;

    public void setResult(String result)
    {
        this.result=result;
    }

    public void setUserMessage(List<UserMessage> userMessages)
    {
        this.userMessages= userMessages;
    }

    public List<UserMessage> getUserMessages()
    {
        return this.userMessages;
    }

     public UpdatePositionResult()
    {


    }
}
