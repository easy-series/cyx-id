package com.example.easyid.examples;

import com.example.easyid.segment.DefaultSegmentId;
import com.example.easyid.segment.IdSegment;
import com.example.easyid.segment.SegmentChainId;
import com.example.easyid.segment.SegmentId;
import com.example.easyid.segment.SegmentIdDistributor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 链式号段ID生成器示例
 * 独立的示例，不依赖Spring
 */
public class SegmentChainIdExample {
    
    public static void main(String[] args) {
        // 创建一个简单的分发器
        SegmentIdDistributor distributor = new SimpleSegmentIdDistributor("test-chain", 1, 10000);
        
        // 创建预取执行器
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        
        try {
            // 创建普通段ID生成器，作为对比
            SegmentId normalSegmentId = new DefaultSegmentId(distributor);
            
            // 创建链式段ID生成器
            SegmentChainId chainSegmentId = new SegmentChainId(distributor, 0.5, executorService);
            
            System.out.println("======== 段ID生成器对比测试 ========");
            
            // 测试普通段ID生成器
            System.out.println("\n普通段ID生成器性能测试:");
            int testCount = 1000000;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < testCount; i++) {
                normalSegmentId.nextId();
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double opsPerSecond = testCount * 1000.0 / duration;
            
            System.out.println("生成 " + testCount + " 个ID");
            System.out.println("耗时: " + duration + " 毫秒");
            System.out.println("性能: " + String.format("%.2f", opsPerSecond) + " ops/s");
            
            // 测试链式段ID生成器
            System.out.println("\n链式段ID生成器性能测试:");
            startTime = System.currentTimeMillis();
            for (int i = 0; i < testCount; i++) {
                chainSegmentId.nextId();
            }
            endTime = System.currentTimeMillis();
            duration = endTime - startTime;
            opsPerSecond = testCount * 1000.0 / duration;
            
            System.out.println("生成 " + testCount + " 个ID");
            System.out.println("耗时: " + duration + " 毫秒");
            System.out.println("性能: " + String.format("%.2f", opsPerSecond) + " ops/s");
            
        } finally {
            // 关闭执行器
            executorService.shutdown();
        }
    }
    
    /**
     * 简单的段ID分发器实现
     */
    static class SimpleSegmentIdDistributor implements SegmentIdDistributor {
        private final String name;
        private final long offset;
        private final int step;
        private long value = 0;
        
        public SimpleSegmentIdDistributor(String name, long offset, int step) {
            this.name = name;
            this.offset = offset;
            this.step = step;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public IdSegment nextSegment() {
            // 简单实现，每次返回一个新的号段
            // 增加value，模拟分布式环境中的号段递增
            value += step;
            return new IdSegment(value - step + offset, step);
        }
        
        @Override
        public long getOffset() {
            return offset;
        }
        
        @Override
        public int getStep() {
            return step;
        }
    }
} 