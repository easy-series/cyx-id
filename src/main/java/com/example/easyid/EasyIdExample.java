package com.example.easyid;

import com.example.easyid.segment.SegmentId;
import com.example.easyid.snowflake.SnowflakeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * Easy-ID框架使用示例
 */
@SpringBootApplication
public class EasyIdExample {
    
    public static void main(String[] args) {
        SpringApplication.run(EasyIdExample.class, args);
    }
    
    @Autowired
    private SnowflakeId snowflakeId;
    
    @Autowired
    private Map<String, SnowflakeId> snowflakeIdMap;
    
    @Autowired
    private Map<String, SegmentId> segmentIdMap;
    
    /**
     * 创建一个启动后执行的Runner，展示ID生成
     */
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("======== Easy-ID 示例 ========");
            
            // 使用默认雪花算法生成ID
            System.out.println("--- 默认雪花算法 ---");
            long snowflakeDefaultId = snowflakeId.nextId();
            System.out.println("生成ID: " + snowflakeDefaultId);
            System.out.println("解析ID: " + snowflakeId.parseId(snowflakeDefaultId));
            
            // 使用命名的雪花算法生成ID
            if (snowflakeIdMap.containsKey("order")) {
                System.out.println("--- 订单雪花算法 ---");
                SnowflakeId orderSnowflakeId = snowflakeIdMap.get("order");
                long orderId = orderSnowflakeId.nextId();
                System.out.println("生成订单ID: " + orderId);
                System.out.println("解析订单ID: " + orderSnowflakeId.parseId(orderId));
            }
            
            if (snowflakeIdMap.containsKey("safeJs")) {
                System.out.println("--- 前端安全雪花算法 ---");
                SnowflakeId safeJsSnowflakeId = snowflakeIdMap.get("safeJs");
                long safeJsId = safeJsSnowflakeId.nextId();
                System.out.println("生成安全ID: " + safeJsId);
                System.out.println("解析安全ID: " + safeJsSnowflakeId.parseId(safeJsId));
            }
            
            // 使用号段模式生成ID
            if (segmentIdMap.containsKey("order")) {
                System.out.println("--- 订单号段模式 ---");
                SegmentId orderSegmentId = segmentIdMap.get("order");
                for (int i = 0; i < 5; i++) {
                    System.out.println("生成订单ID: " + orderSegmentId.nextId());
                }
            }
            
            if (segmentIdMap.containsKey("product")) {
                System.out.println("--- 商品号段模式 ---");
                SegmentId productSegmentId = segmentIdMap.get("product");
                for (int i = 0; i < 5; i++) {
                    System.out.println("生成商品ID: " + productSegmentId.nextId());
                }
            }
            
            System.out.println("========================");
        };
    }
} 