package com.paylog.app;

public interface BotResponseCallback {
    void onResponse(String response);
    void onError(Throwable t);
}
