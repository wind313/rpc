package com.yjc.rateLimit.impl;

import com.yjc.rateLimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {

    private static int RATE;
    private static int CAPACITY;
    private volatile int curCapacity;
    private volatile long timeStamp=System.currentTimeMillis();

    public TokenBucketRateLimitImpl(int rate, int capacity) {
        RATE = rate;
        CAPACITY = capacity;
        curCapacity = capacity;
    }

    @Override
    public synchronized boolean getToken() {
        if (curCapacity > 0) {
            curCapacity--;
            return true;
        }
        long now = System.currentTimeMillis();
        if(now - timeStamp >= RATE){
            if((now - timeStamp)/RATE > 1){
                curCapacity += (int)(curCapacity-timeStamp)/RATE-1;
            }
            if(curCapacity>CAPACITY) curCapacity = CAPACITY;
            timeStamp = now;
            return true;
        }
        return false;
    }
}
