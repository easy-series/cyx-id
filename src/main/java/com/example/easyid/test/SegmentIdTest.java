package com.example.easyid.test;

import com.example.easyid.core.Clock;
import com.example.easyid.core.SystemClock;
import com.example.easyid.segment.DefaultSegmentId;
import com.example.easyid.segment.IdSegment;
import com.example.easyid.segment.SegmentId;
import com.example.easyid.segment.SegmentIdDistributor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

/**
 * 段ID生成器测试类
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackageClasses = SegmentIdTest.class)
public class SegmentIdTest {
    
    public static void main(String[] args) {
        SpringApplication.run(SegmentIdTest.class, args);
    }
    
    @Bean
    public Clock systemClock() {
        return new SystemClock();
    }
    
    @Bean
    public SegmentIdDistributor testSegmentIdDistributor() {
        // 创建一个简单的分发器，用于测试
        return new SegmentIdDistributor() {
            @Override
            public String getName() {
                return "test-segment";
            }
            
            @Override
            public IdSegment nextSegment() {
                // 返回一个固定的号段，从1开始，步长10000
                return new IdSegment(1, 10000);
            }
            
            @Override
            public long getOffset() {
                return 0;
            }
            
            @Override
            public int getStep() {
                return 10000;
            }
        };
    }
    
    @Bean
    public Map<String, SegmentId> segmentIdMap(SegmentIdDistributor testSegmentIdDistributor) {
        Map<String, SegmentId> map = new HashMap<>();
        map.put("test-segment", new DefaultSegmentId(testSegmentIdDistributor));
        return map;
    }
    
    @Bean
    public CommandLineRunner segmentIdTestRunner(Map<String, SegmentId> segmentIdMap) {
        return args -> {
            System.out.println("======== 段ID生成测试 ========");
            
            SegmentId segmentId = segmentIdMap.get("test-segment");
            
            System.out.println("生成10个ID:");
            for (int i = 0; i < 10; i++) {
                long id = segmentId.nextId();
                System.out.println("ID-" + i + ": " + id);
            }
            
            System.out.println("\n性能测试:");
            int testCount = 1000000;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < testCount; i++) {
                segmentId.nextId();
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