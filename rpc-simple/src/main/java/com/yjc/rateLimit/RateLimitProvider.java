package com.yjc.rateLimit;

import com.yjc.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {
    private Map<String, RateLimit> rateLimitMap = new HashMap<>();
    public RateLimit getRateLimit(String interfaceName) {
        if(!rateLimitMap.containsKey(interfaceName)){
            RateLimit rateLimit = new TokenBucketRateLimitImpl(1000, 1);
            rateLimitMap.put(interfaceName, rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
