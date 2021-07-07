package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上传地理位置
     *
     * @param loginUserId
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    public void addLocation(Long loginUserId, Double latitude, Double longitude, String addrStr) {
        //1.获取当前系统时间
        long nowTime = System.currentTimeMillis();

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        //如果登录用户的地理位置记录已经存在
        if (mongoTemplate.exists(query, UserLocation.class)) {
            //则更新
            Update update = new Update();
            //更新当前时间
            update.set("updated", nowTime);
            //更新 最近一次更新的时间
            update.set("lastUpdated", nowTime);
            //更新位置描述
            update.set("address", addrStr);
            //更新经纬度
            update.set("location", new GeoJsonPoint(latitude, longitude));
            mongoTemplate.updateFirst(query, update, UserLocation.class);
        } else {
            //登录用户的地理位置记录不存在,则添加
            UserLocation userLocation = new UserLocation();

            userLocation.setId(ObjectId.get());
            userLocation.setUserId(loginUserId);
            userLocation.setAddress(addrStr);
            userLocation.setCreated(nowTime);
            userLocation.setUpdated(nowTime);
            userLocation.setLastUpdated(nowTime);

            userLocation.setLocation(new GeoJsonPoint(latitude, longitude));

            mongoTemplate.save(userLocation);
        }

    }

    /**
     * 搜附近
     *
     * @param miles
     * @param loginUserId
     * @return
     */
    public List<UserLocationVo> searchNearBy(Long loginUserId, Long miles) {
        //1、根据用户id，查询当前用户的位置
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);

        //2、指定查询的半径范围
        GeoJsonPoint location = userLocation.getLocation();
        Distance distance = new Distance(miles / 1000, Metrics.KILOMETERS);

        //3、根据此半径画圆
        Circle circle = new Circle(location, distance);  //圆点，半径

        //4.调用mongotemplate查询 List<UserLocation>
        Query nearQuery = new Query();

        nearQuery.addCriteria(Criteria.where("location").withinSphere(circle));

        List<UserLocation> userLocations = mongoTemplate.find(nearQuery, UserLocation.class);

        //5、转化为List<UserLocationVo>
        return UserLocationVo.formatToList(userLocations);

    }
}
