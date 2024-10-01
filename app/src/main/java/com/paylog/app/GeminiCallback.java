package com.paylog.app;

public interface GeminiCallback {
    void onResult(String result);
    void onError(Throwable throwable);
}
