package experiment.clickhouse.service;

import java.time.LocalDateTime;

public class ArticleViewEvent {
    private Double postId;
    private LocalDateTime viewTime;
    private String clientId;

    public ArticleViewEvent() {
    }

    public ArticleViewEvent(Double postId, LocalDateTime viewTime, String clientId) {
        this.postId = postId;
        this.viewTime = viewTime;
        this.clientId = clientId;
    }

    public Double getPostId() {
        return postId;
    }

    public void setPostId(Double postId) {
        this.postId = postId;
    }

    public LocalDateTime getViewTime() {
        return viewTime;
    }

    public void setViewTime(LocalDateTime viewTime) {
        this.viewTime = viewTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "ArticleViewEvent{" +
                "postId=" + postId +
                ", viewTime=" + viewTime +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}