package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {

    /**
     * 上传地理位置
     * @param loginUserId
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    void addLocation(Long loginUserId, Double latitude, Double longitude, String addrStr);

    /**
     * 搜附近
     * @param distance
     * @param loginUserId
     * @return
     */
    List<UserLocationVo> searchNearBy(Long loginUserId, Long miles);
}
