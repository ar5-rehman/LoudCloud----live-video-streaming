package com.tech.loudcloud.UserPackage.ArchivedVideosService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideosPojo {

    @SerializedName("streamName")
    @Expose
    private String streamName;
    @SerializedName("vodName")
    @Expose
    private String vodName;
    @SerializedName("streamId")
    @Expose
    private String streamId;
    /*@SerializedName("creationDate")
    @Expose
    private int creationDate;
    @SerializedName("duration")
    @Expose
    private int duration;
    @SerializedName("fileSize")
    @Expose
    private int fileSize;*/
    @SerializedName("filePath")
    @Expose
    private String filePath;
    @SerializedName("vodId")
    @Expose
    private String vodId;
    @SerializedName("type")
    @Expose
    private String type;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getVodName() {
        return vodName;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

   /* public Integer getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Integer creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }*/

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVodId() {
        return vodId;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}