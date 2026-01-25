package com.yow.access.dto;

public class TenantStatsResponse {

    private long userCount;
    private long resourceCount;

    public TenantStatsResponse() {
    }

    public TenantStatsResponse(long userCount, long resourceCount) {
        this.userCount = userCount;
        this.resourceCount = resourceCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(long resourceCount) {
        this.resourceCount = resourceCount;
    }
}
