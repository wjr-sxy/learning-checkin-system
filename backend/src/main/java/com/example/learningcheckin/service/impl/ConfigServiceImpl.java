package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.learningcheckin.entity.SysConfig;
import com.example.learningcheckin.mapper.SysConfigMapper;
import com.example.learningcheckin.service.IConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl implements IConfigService {

    @Autowired
    private SysConfigMapper configMapper;

    private static final String KEY_POINTS_MULTIPLIER = "points_multiplier";

    @Override
    public String getConfig(String key) {
        SysConfig config = configMapper.selectOne(new QueryWrapper<SysConfig>().eq("config_key", key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void setConfig(String key, String value, String description) {
        SysConfig config = configMapper.selectOne(new QueryWrapper<SysConfig>().eq("config_key", key));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            configMapper.insert(config);
        } else {
            config.setConfigValue(value);
            if (description != null) {
                config.setDescription(description);
            }
            configMapper.updateById(config);
        }
    }

    @Override
    public double getPointsMultiplier() {
        String val = getConfig(KEY_POINTS_MULTIPLIER);
        if (val == null) return 1.5; // Default 1.5
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 1.5;
        }
    }

    @Override
    public void setPointsMultiplier(double multiplier) {
        setConfig(KEY_POINTS_MULTIPLIER, String.valueOf(multiplier), "Global Points Multiplier");
    }
}
