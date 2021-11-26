package com.tech.loudcloud.UserPackage.pojoClasses;

public class VideosLinksPojo {

    public VideosLinksPojo(String link, String vodName)
    {
        this.link = link;
        this.streamName = vodName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    String link;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    String streamName;

}
