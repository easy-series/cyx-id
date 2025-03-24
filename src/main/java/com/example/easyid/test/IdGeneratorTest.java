package com.example.easyid.test;

import com.example.easyid.core.Clock;
import com.example.easyid.core.SafeClock;
import com.example.easyid.core.SystemClock;
import com.example.easyid.snowflake.DefaultSnowflakeId;
import com.example.easyid.snowflake.SnowflakeId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * ID生成器测试类
 */
@SpringBootApplication
public class IdGeneratorTest {
    
    public static void main(String[] args) {
        SpringApplication.run(IdGeneratorTest.class, args);
    }
    
    @Bean
    public Clock systemClock() {
        return new SystemClock();
    }
    
    @Bean
    public Clock safeClock(Clock systemClock) {
        return new SafeClock(systemClock, 10, 2000);
    }
    
    @Bean
    public SnowflakeId snowflakeId(Clock safeClock) {
        return new DefaultSnowflakeId("test-snowflake", safeClock, 1, 41, 10, 12);
    }
    
    @Bean
    public CommandLineRunner idGeneratorTestRunner(SnowflakeId snowflakeId) {
        return args -> {
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
            
            System.out.println("\n测试完成，应用将退出");
            System.exit(0);
        };
    }
} 