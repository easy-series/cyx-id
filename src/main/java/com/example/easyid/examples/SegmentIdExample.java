package com.example.easyid.examples;

import com.example.easyid.segment.DefaultSegmentId;
import com.example.easyid.segment.IdSegment;
import com.example.easyid.segment.SegmentId;
import com.example.easyid.segment.SegmentIdDistributor;

/**
 * 段ID生成器示例
 * 独立的示例，不依赖Spring
 */
public class SegmentIdExample {
    
    public static void main(String[] args) {
        // 创建一个简单的分发器
        SegmentIdDistributor distributor = new SimpleSegmentIdDistributor("test", 1, 10000);
        
        // 创建段ID生成器
        SegmentId segmentId = new DefaultSegmentId(distributor);
        
        System.out.println("======== 段ID生成测试 ========");
        
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