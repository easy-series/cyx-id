package com.example.easyid.examples;

import com.example.easyid.core.Clock;
import com.example.easyid.core.SafeClock;
import com.example.easyid.core.SystemClock;
import com.example.easyid.snowflake.DefaultSnowflakeId;
import com.example.easyid.snowflake.SnowflakeId;

/**
 * 雪花算法示例
 * 独立的示例，不依赖Spring
 */
public class SnowflakeIdExample {
    
    public static void main(String[] args) {
        // 创建时钟
        Clock clock = new SystemClock();
        Clock safeClock = new SafeClock(clock, 10, 2000);
        
        // 创建雪花算法ID生成器
        SnowflakeId snowflakeId = new DefaultSnowflakeId("example", safeClock, 1, 41, 10, 12);
        
        System.out.println("======== 雪花算法ID生成测试 ========");
        
        System.out.println("生成10个ID:");
        for (int i = 0; i < 10; i++) {
            long id = snowflakeId.nextId();
            System.out.println("ID-" + i + ": " + id);
            System.out.println("解析: " + snowflakeId.parseId(id));
        }
        
        System.out.println("\n性能测试:");
        int testCount = 1000000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            snowflakeId.nextId();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double opsPerSecond = testCount * 1000.0 / duration;
        
        System.out.println("生成 " + testCount + " 个ID");
        System.out.println("耗时: " + duration + " 毫秒");
        System.out.println("性能: " + String.format("%.2f", opsPerSecond) + " ops/s");
    }
} 