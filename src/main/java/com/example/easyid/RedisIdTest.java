package com.example.easyid;

import com.example.easyid.segment.SegmentId;
import com.example.easyid.snowflake.SnowflakeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * Redis ID测试类
 * 用于测试Redis连接和ID生成功能
 */
@SpringBootApplication
public class RedisIdTest {
    
    public static void main(String[] args) {
        SpringApplication.run(RedisIdTest.class, args);
    }
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private SnowflakeId snowflakeId;
    
    @Autowired
    private Map<String, SnowflakeId> snowflakeIdMap;
    
    @Autowired
    private Map<String, SegmentId> segmentIdMap;
    
    /**
     * 创建测试Runner
     */
    @Bean
    public CommandLineRunner testRunner() {
        return args -> {
            System.out.println("======== Easy-ID Redis测试 ========");
            
            // 测试Redis连接
            System.out.println("正在测试Redis连接...");
            try {
                String testKey = "easy-id:test:key";
                String testValue = "test-" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(testKey, testValue);
                String fetchedValue = redisTemplate.opsForValue().get(testKey);
                
                if (testValue.equals(fetchedValue)) {
                    System.out.println("Redis连接测试成功!");
                } else {
                    System.out.println("Redis连接测试失败: 值不匹配");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Redis连接测试失败: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            
            // 测试雪花算法ID
            System.out.println("\n--- 雪花算法 (Redis机器号) ---");
            for (int i = 0; i < 5; i++) {
                long id = snowflakeId.nextId();
                System.out.println("ID-" + i + ": " + id);
                System.out.println("解析: " + snowflakeId.parseId(id));
            }
            
            // 测试号段模式ID
            if (segmentIdMap.containsKey("order")) {
                System.out.println("\n--- 订单号段模式 (Redis分配) ---");
                SegmentId orderSegmentId = segmentIdMap.get("order");
                for (int i = 0; i < 10; i++) {
                    System.out.println("订单ID-" + i + ": " + orderSegmentId.nextId());
                }
            }
            
            if (segmentIdMap.containsKey("product")) {
                System.out.println("\n--- 商品号段模式 (Redis分配) ---");
                SegmentId productSegmentId = segmentIdMap.get("product");
                for (int i = 0; i < 10; i++) {
                    System.out.println("商品ID-" + i + ": " + productSegmentId.nextId());
                }
            }
            
            System.out.println("\n测试完成，应用将退出...");
            System.exit(0);
        };
    }
} 