package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.AnnouncementMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AnnouncementApiImpl implements AnnouncementApi {

    @Autowired
    private AnnouncementMapper announcementMapper;


    /**
     * 分页查询 公告列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPage(Long page, Long pageSize) {

        //分页的参数
        IPage<Announcement> IPage = new Page<>(page, pageSize);

        //查询的条件
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();

        //分页查询
        announcementMapper.selectPage(IPage, queryWrapper);

        //构建返回的PageResult
        PageResult<Announcement> pageResult = new PageResult<>();

        //设置分页结果
        pageResult.setItems(IPage.getRecords());

        //设置总记录数
        pageResult.setCounts(IPage.getTotal());

        //设置总页数
        pageResult.setPages(IPage.getPages());

        //设置每页显示多少条
        pageResult.setPagesize(pageSize);

        //设置当前页码
        pageResult.setPage(page);


        return pageResult;

    }


    /**
     * 根据id批量查询所有公告
     *
     * @param announcementIds
     * @return
     */
    public List<Announcement> findByBatchIds(List<String> announcementIds) {

        return announcementMapper.selectBatchIds(announcementIds);
    }
}
