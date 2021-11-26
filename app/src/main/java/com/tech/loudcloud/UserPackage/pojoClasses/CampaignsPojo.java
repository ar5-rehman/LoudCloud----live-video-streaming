package com.tech.loudcloud.UserPackage.pojoClasses;

public class CampaignsPojo {

    public CampaignsPojo(String artistPic, String artistName, String location, String listOfSongs, String alternativeSongs, String liveTime, String votesEarned, String votesNeeded, String alternativeLocation){

        this.artistPic = artistPic;
        this.artistName = artistName;
        this.location = location;
        this.listOfSongs = listOfSongs;
        this.alternativeSongs = alternativeSongs;
        this.liveTime = liveTime;
        this.votesEarned = votesEarned;
        this.votesNeeded = votesNeeded;
        this.alternativeLocation = alternativeLocation;
    }

    public String getVotesEarned() {
        return votesEarned;
    }

    public void setVotesEarned(String votesEarned) {
        this.votesEarned = votesEarned;
    }

    public String getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(String liveTime) {
        this.liveTime = liveTime;
    }

    public String getVotesNeeded() {
        return votesNeeded;
    }

    public void setVotesNeeded(String votesNeeded) {
        this.votesNeeded = votesNeeded;
    }

    public String getListOfSongs() {
        return listOfSongs;
    }

    public void setListOfSongs(String listOfSongs) {
        this.listOfSongs = listOfSongs;
    }

    public String getAlternativeSongs() {
        return alternativeSongs;
    }

    public void setAlternativeSongs(String alternativeSongs) {
        this.alternativeSongs = alternativeSongs;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistPic() {
        return artistPic;
    }

    public void setArtistPic(String artistPic) {
        this.artistPic = artistPic;
    }

    String artistPic;
    String artistName;
    String location;
    String listOfSongs;
    String alternativeSongs;

    public String getAlternativeLocation() {
        return alternativeLocation;
    }

    public void setAlternativeLocation(String alternativeLocation) {
        this.alternativeLocation = alternativeLocation;
    }

    String alternativeLocation;
    String liveTime;
    String votesEarned;
    String votesNeeded;

}
