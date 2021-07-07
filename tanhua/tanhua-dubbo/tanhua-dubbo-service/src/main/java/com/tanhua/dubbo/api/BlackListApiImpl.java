package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class BlackListApiImpl implements BlackListApi {

    @Autowired
    private BlackListMapper blackListMapper;


    /**
     * 分页查询
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult fingPage(Long loginUserId, Long page, Long pageSize) {
        //分页的参数
        IPage<BlackList> blackListIPage = new Page<>(page, pageSize);

        //查询的条件
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUserId);

        //分页查询
        blackListMapper.selectPage(blackListIPage, queryWrapper);

        //构建返回的PageResult
        PageResult<BlackList> pageResult = new PageResult<>();

        //设置分页结果
        pageResult.setItems(blackListIPage.getRecords());

        //设置当前页码
        pageResult.setPage(page);

        //设置总记录数
        pageResult.setCounts(blackListIPage.getTotal());

        //设置总页数
        pageResult.setPages(blackListIPage.getPages());

        //设置每页大小
        pageResult.setPagesize(blackListIPage.getSize());

        return pageResult;
    }


    /**
     * 移除黑名单
     * @param loginUserId
     * @param blackUserId
     */
    public void delete(Long loginUserId, Long blackUserId) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.eq("black_user_id", blackUserId);

        /*
         相当于:
          delete from tb_black_list WHERE user_id=? and black_user_id=?;
         */
        blackListMapper.delete(queryWrapper);
    }
}
