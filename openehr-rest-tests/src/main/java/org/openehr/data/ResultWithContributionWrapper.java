/*
 * Copyright (C) 2020 Better d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openehr.data;

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
