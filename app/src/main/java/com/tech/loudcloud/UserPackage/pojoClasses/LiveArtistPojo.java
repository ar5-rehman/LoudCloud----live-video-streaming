package com.tech.loudcloud.UserPackage.pojoClasses;

public class LiveArtistPojo {

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getArtistImageUrl() {
        return artistImageUrl;
    }

    public void setArtistImageUrl(String artistImageUrl) {
        this.artistImageUrl = artistImageUrl;
    }

    String channelName;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    String artistId;
    String artistImageUrl;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    String artistName;

    public LiveArtistPojo(String artistId, String channelName,String artistName, String artistImageUrl)
    {
        this.artistId = artistId;
        this.channelName = channelName;
        this.artistImageUrl = artistImageUrl;
        this.artistName = artistName;
    }
}
