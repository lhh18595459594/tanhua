package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class SettingsApiImpl implements SettingsApi {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 通过id查询用户的【通知设置】
     *
     * @return
     */
    public Settings findByUserId(Long userId) {

        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        return settingsMapper.selectOne(queryWrapper);

    }

    /**
     * 更新【通知设置】
     *
     * @param settings
     */
    public void update(Settings settings) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", settings.getUserId());

        settingsMapper.update(settings, queryWrapper);
    }

    /**
     * 添加通知设置
     *
     * @param settings
     */
    public void add(Settings settings) {
        settingsMapper.insert(settings);
    }
}
