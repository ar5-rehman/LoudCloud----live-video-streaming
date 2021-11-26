package com.tech.loudcloud.ArtistPackage.pojoClasses;

public class ArtistCampaignData {

    String artistName;

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
    String listOfSongs;
    String alternativeSongs;

    public String getAlternativeLocations() {
        return alternativeLocations;
    }

    public void setAlternativeLocations(String alternativeLocations) {
        this.alternativeLocations = alternativeLocations;
    }

    String alternativeLocations;
    String liveTime;

    public int getVotesEarned() {
        return votesEarned;
    }

    public void setVotesEarned(int votesEarned) {
        this.votesEarned = votesEarned;
    }

    int votesEarned;

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

    String votesNeeded;

    public int getPerformanceOneVote() {
        return performanceOneVote;
    }

    public void setPerformanceOneVote(int performanceOneVote) {
        this.performanceOneVote = performanceOneVote;
    }

    public int getPerformanceTwoVote() {
        return performanceTwoVote;
    }

    public void setPerformanceTwoVote(int performanceTwoVote) {
        this.performanceTwoVote = performanceTwoVote;
    }

    public int getLocationOneVote() {
        return locationOneVote;
    }

    public void setLocationOneVote(int locationOneVote) {
        this.locationOneVote = locationOneVote;
    }

    public int getLocationTwoVote() {
        return locationTwoVote;
    }

    public void setLocationTwoVote(int locationTwoVote) {
        this.locationTwoVote = locationTwoVote;
    }

    int performanceOneVote;
    int performanceTwoVote;
    int locationOneVote;
    int locationTwoVote;

    public ArtistCampaignData(){

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

    String location;
}
