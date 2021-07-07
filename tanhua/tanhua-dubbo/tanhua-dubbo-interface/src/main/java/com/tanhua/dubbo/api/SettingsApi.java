package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

public interface SettingsApi {

    /**
     * 读取用户的【通知设置】
     * @return
     */
    Settings findByUserId(Long userId);

    /**
     * 更新【通知设置】
     * @param settings
     */
    void update(Settings settings);

    /**
     * 添加通知设置
     * @param settings
     */
    void add(Settings settings);
}
