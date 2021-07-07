package com.tanhua.server.service;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.RecommendUserVo;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 【今日佳人】 业务层
 */
@Service
@Slf4j
public class TodayBestServcie {

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    /**
     * 今日佳人
     *
     * @return
     */
    public TodayBestVo queryTodayBest() {
        //1.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        log.info("今日佳人查询：登陆用户的id={}", loginUserId);

        // 2. 调用recommendUserApi查询佳人，缘分值最高的.
        RecommendUser recommendUser = recommendUserApi.todayBest(loginUserId);

        log.info("查询到的佳人：{}", recommendUser);

        Long todayBestUserId = 99l; // 默认的佳人
        Long fateValue = 80l; // 默认的分数

        //3. 判断是否有推荐用户(佳人)
        if (null != recommendUser) {
            todayBestUserId = recommendUser.getUserId();
            fateValue = recommendUser.getScore().longValue();
        }
        //5. 查询这个佳人的详细信息
        UserInfo userInfo = userInfoApi.findById(todayBestUserId);

        //6. 构建vo
        TodayBestVo todayBestVo = new TodayBestVo();

        //7.复制属性值
        BeanUtils.copyProperties(userInfo, todayBestVo);
        todayBestVo.setTags(StringUtils.split(userInfo.getTags(),","));

        //8.设置缘分值
        todayBestVo.setFateValue(fateValue);


        return todayBestVo;

    }

    /**
     * 交友——推荐用户列表
     *
     * @param queryParam
     * @return
     */
    public PageResult<RecommendUserVo> recommendationList(RecommendUserQueryParam queryParam) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //2.调用mongo api查询推荐用户列表，实现分页查询，按分数降序  Page:当前页数   PageSize：每页显示的条数
        PageResult pageResult = recommendUserApi.findPage(loginUserId, queryParam.getPage(), queryParam.getPagesize());

        //3.查询出来的【所有推荐用户】的结果封装在items里面，形成一个集合
        List<RecommendUser> recommendUsers = pageResult.getItems();

        //4.判断是否有推荐用户，如果没有则要生成默认的推荐用户
        if ((CollectionUtils.isEmpty(recommendUsers))) {
            //如果为空，则生成默认推荐用户
            recommendUsers = getDefaultRecommendUser();  //把id= 1~10 给他。
            //如果为空，则分页信息全是0，需要补全分页信息
            pageResult.setCounts(10l);   //给他补齐10条总记录数
            pageResult.setPage(1l);    // 1页
        }

        //5.遍历 recommendUsers，从recommendUsers里面单独把所有推荐用户id取出来，组成list集合recommendUserIds
        List<Long> recommendUserIds = recommendUsers.stream().map(RecommendUser::getUserId).collect(Collectors.toList());

        //6.取出id以后，调用api批量查询。得到所有的用户详细信息集合userInfoList
        List<UserInfo> userInfoList = userInfoApi.findByBatchIds(recommendUserIds);

        /*7.【通过流的方式遍历并转换】
            将上面的userInfoList集合对象转换成Map格式,key=用户id, value=用户信息的形式存入map中
            把集合中每一个元素都进行这样的处理(UserInfo::getId, userInfo -> userInfo)，

            collect：收集起来
            参数1：UserInfo::getId 取出每个元素的Id
            参数2：userInfo（每个元素的变量名） -> userInfo（相当于return userInfo）

            【每取出一个推荐用户的Id，就返回相关的信息,通过key-value形式存入map】!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         */
        Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
       /*
        上面的代码，相当于这段代码
        Map<Long, UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getId(), userInfo);
        }

        */


        //8.遍历【所有推荐用户】recommendUsers 的结果，
        List<RecommendUserVo> voList = recommendUsers.stream().map(recommendUser -> {
            RecommendUserVo recommendUserVo = new RecommendUserVo();

            /*
               9.这里本应该遍历第6步的userInfoList【每一个推荐用户的所有信息】，
               但是 第7步 已经用 Map 存入了id和信息，这里只需要通过推荐用户id取出推荐的用户信息即可，这里不需要再遍历
             */
            Long userId = recommendUser.getUserId();
            UserInfo userInfo = userInfoMap.get(userId);
            //10.将每个用户的信息userInfo，用与前端交互的recommendUserVo实体类接收。并返回给前端展示。
            BeanUtils.copyProperties(userInfo, recommendUserVo);

            //11.tag标签处理
            recommendUserVo.setTags(StringUtils.split(userInfo.getTags(), ","));

            //12.设置缘分值,longvalue会去掉小数点，不要四舍五入
            recommendUserVo.setFateValue(recommendUser.getScore().longValue());
            return recommendUserVo;
        }).collect(Collectors.toList());


        //13.把转换后的voList设置到pageResult中
        pageResult.setItems(voList);

        return pageResult;
    }


    /**
     * 默认推荐用户
     */
    private List<RecommendUser> getDefaultRecommendUser() {
        List<RecommendUser> list = new ArrayList<>();
        for (long i = 1; i < 10; i++) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(i);

            //设置分数随机在70到98之间
            recommendUser.setScore(RandomUtils.nextDouble(70, 98));

            list.add(recommendUser);
        }
        return list;
    }


    /**
     * 查看佳人信息
     *
     * @param userId
     * @return
     */
    public TodayBestVo queryUserDetail(Long userId) {

        //1.调用api查看佳人的详情
        UserInfo userInfo = userInfoApi.findById(userId);

        //2.构建vo
        TodayBestVo todayBestVo = new TodayBestVo();

        //3.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        //3.查询佳人的缘分值
        Double score = recommendUserApi.queryForScore(userId, loginUserId);

        //4.把佳人信息 复制到todayBestVo中
        BeanUtils.copyProperties(userInfo, todayBestVo);

        //5.设置标签
        todayBestVo.setTags(StringUtils.split(userInfo.getTags(), ","));

        //6.设置缘分值
        todayBestVo.setFateValue(score.longValue());

        return todayBestVo;
    }

    /**
     * 查询陌生人的问题
     *
     * @param userId
     * @return
     */
    public String strangerQuestions(Long userId) {

        //1.调用api，根据id查询陌生人的问题
        Question question = questionApi.findByUserId(userId);

        //2.判断陌生人信息是否为空，或陌生人问题是否存在
        if (null == question || StringUtils.isEmpty(question.getTxt())) {
            //3.如果上面有一个为空，则设置默认问题
            return "你喜欢我吗?";
        }

        //4.不然就返回陌生人问题信息
        return question.getTxt();

    }
}
