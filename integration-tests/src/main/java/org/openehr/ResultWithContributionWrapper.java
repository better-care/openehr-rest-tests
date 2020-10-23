package org.openehr;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Dusan Markovic
 */
public class ResultWithContributionWrapper<T> {
    private Map<String, OffsetDateTime> contributionUidCommittedTimestampMap;
    private T resultObject;
    private Future<Boolean> eventFuture;
    // flag deleted is needed to distinct between not found and deleted resultObject which is null in both cases
    private boolean deleted;

    public ResultWithContributionWrapper() {
    }

    public ResultWithContributionWrapper(T resultObject) {
        this.resultObject = resultObject;
    }

    public T getResultObject() {
        return resultObject;
    }

    public ResultWithContributionWrapper<T> withResultObject(T resultObject) {
        this.resultObject = resultObject;
        return this;
    }

    public Future<Boolean> getEventFuture() {
        return eventFuture;
    }

    public void setEventFuture(Future<Boolean> eventFuture) {
        this.eventFuture = eventFuture;
    }

    public Map<String, OffsetDateTime> getContributionUidCommittedTimestampMap() {
        return contributionUidCommittedTimestampMap;
    }

    public ResultWithContributionWrapper<T> withContributionUidCommittedTimestampMap(Map<String, OffsetDateTime> contributionUidCommittedTimestampMap) {
        this.contributionUidCommittedTimestampMap = contributionUidCommittedTimestampMap;
        return this;
    }

    public ResultWithContributionWrapper<T> withContributionUidCommittedTimestamp(String contributionUid, OffsetDateTime committedTimestamp) {
        if (contributionUidCommittedTimestampMap == null) {
            contributionUidCommittedTimestampMap = new HashMap<>();
        }
        contributionUidCommittedTimestampMap.put(contributionUid, committedTimestamp);
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public ResultWithContributionWrapper<T> markDeleted() {
        deleted = true;
        return this;
    }
}
