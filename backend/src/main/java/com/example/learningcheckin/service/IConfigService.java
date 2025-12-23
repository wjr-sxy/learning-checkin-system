package com.example.learningcheckin.service;

public interface IConfigService {
    String getConfig(String key);
    void setConfig(String key, String value, String description);
    double getPointsMultiplier();
    void setPointsMultiplier(double multiplier);
}
