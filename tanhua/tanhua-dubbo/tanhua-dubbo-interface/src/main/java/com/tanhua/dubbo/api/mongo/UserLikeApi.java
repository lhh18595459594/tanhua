package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface UserLikeApi {

    /**
     * 查询用户相互喜欢的好友个数，统计好友数
     * @param loginUserId
     * @return
     */
    Long countLikeEachOther(Long loginUserId);

    /**
     * 统计【我喜欢的 】用户，统计个数
     * @param loginUserId
     * @return
     */
    Long countOneSideLike(Long loginUserId);

    /**
     * 统计【我的粉丝】个数
     * @param loginUserId
     * @return
     */
    Long countFens(Long loginUserId);

    /**
     * 分页查询相互喜欢的
     * @param loginUserId
     * @param page
     * @param pagesize
     */
    PageResult findPageLikeEachOther(Long loginUserId, Long page, Long pagesize);

    /**
     * 分页查询我喜欢的
     * @param loginUserId
     * @param page
     * @param pagesize
     */
    PageResult findPageOneSideLike(Long loginUserId, Long page, Long pagesize);

    /**
     * 分页查询喜欢我的粉丝
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageFens(Long loginUserId, Long page, Long pagesize);

    /**
     * 分页查询谁看过我 列表
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageMyVisitors(Long loginUserId, Long page, Long pagesize);

    /**
     * 粉丝中的喜欢
     *
     * @param fansId
     */
    Boolean fansLike(Long loginUserId, Long fansId);
}
