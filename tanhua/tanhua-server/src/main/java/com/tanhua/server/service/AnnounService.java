package com.tanhua.server.service;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.AnnouncementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AnnounService {

    @Reference
    private AnnouncementApi announcementApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 查看公告列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<AnnouncementVo> announcements(Long page, Long pageSize) {

        //1.调用api，查询公告
        PageResult pageResult = announcementApi.findPage(page, pageSize);

        //2.获取所有的公告对象信息
        List<Announcement> records = pageResult.getItems();



        //3.遍历所有公告对象信息，取出每条公告对应的id。
        List<String> announcementIds = records.stream().map(Announcement::getId).collect(Collectors.toList());

        //4.遍历公告对应的Id，批量查询，查出所有的公告内容
        List<Announcement> announcementInfo = announcementApi.findByBatchIds(announcementIds);

        //5.遍历所有的公告内容信息。并将对应的id和内容，以key-value的形式存进map集合中
        Map<String, Announcement> anInfoMap = announcementInfo.stream().collect(Collectors.toMap(Announcement::getId, announcement -> announcement));

        //6.遍历
        List<AnnouncementVo> voList = records.stream().map(announcement -> {

            //7.取出每一个对应id的公告内容
            Announcement anInfo = anInfoMap.get(announcement.getId());

            //8.构建vo
            AnnouncementVo Vo = new AnnouncementVo();

            //9.复制信息
            BeanUtils.copyProperties(anInfo, Vo);

            return Vo;
        }).collect(Collectors.toList());

        pageResult.setItems(voList);

        return pageResult;
    }


}
