package com.yjc.extension;

import lombok.Data;

@Data
public class Holder<T> {

    private volatile T value;

}
