package com.lushihao.ewhatbackend;

import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.service.CanteenService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lushihao.ewhatbackend.constant.RedisConstants.SCHOOL_GEO_KEY;

@SpringBootTest
class EwhatBackendApplicationTests {

    @Resource
    private CanteenService canteenService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void contextLoads() {
    }

    // 加载canteen中的xy数据到redis中
    @Test
    void loadCanteenData(){
        // 获取所有的食堂信息
        List<Canteen> canteens = canteenService.list();
        // 按照学校分组 将schoolId一致的放到一个集合中
        Map<Long, List<Canteen>> map = canteens.stream().collect(Collectors.groupingBy(Canteen::getSchoolId));
        // 分批写入Redis中
        for (Map.Entry<Long, List<Canteen>> entry : map.entrySet()) {
            Long schoolId = entry.getKey();
            String key = SCHOOL_GEO_KEY + schoolId;
            // 获取相同学校的食堂集合
            List<Canteen> canteensList = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(canteensList.size());
            for (Canteen canteen : canteensList) {
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        canteen.getId().toString(),
                        new Point(canteen.getX(), canteen.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }


}
