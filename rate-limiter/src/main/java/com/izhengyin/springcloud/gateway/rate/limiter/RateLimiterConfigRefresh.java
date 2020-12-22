package com.izhengyin.springcloud.gateway.rate.limiter;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScope;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-12-22 16:58
 */
@Slf4j
public class RateLimiterConfigRefresh {
    private final RefreshScope refreshScope;
    public RateLimiterConfigRefresh(RefreshScope refreshScope) {
        this.refreshScope = refreshScope;
    }
    /**
     * 刷新bean
     */
    public void refresh() {
        Preconditions.checkState(refreshScope.refresh(BeanName.RATE_LIMITER_PROPERTIES),"refresh fail ",BeanName.RATE_LIMITER_PROPERTIES);
        Preconditions.checkState(refreshScope.refresh(BeanName.RATE_LIMITER_CONFIG),"refresh fail ",BeanName.RATE_LIMITER_CONFIG);
        Preconditions.checkState(refreshScope.refresh(BeanName.RATE_LIMITER_CONFIG_UTILS),"refresh fail ",BeanName.RATE_LIMITER_CONFIG_UTILS);
        Preconditions.checkState(refreshScope.refresh(BeanName.RATE_LIMITER_FILTER),"refresh fail ",BeanName.RATE_LIMITER_FILTER);
    }
}
