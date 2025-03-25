package com.caoyixin.cyxid.benchmark.segment;

import com.caoyixin.cyxid.benchmark.core.IdGeneratorBenchmark;
import com.caoyixin.cyxid.segment.SegmentIdGenerator;
import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分段ID生成器基准测试
 */
public class SegmentIdGeneratorBenchmark extends IdGeneratorBenchmark {
    
    private static final String GENERATOR_NAME = "benchmark";
    private static final int SEGMENT_SIZE = 1000;
    
    // 用于模拟ID段分配的当前值
    private final AtomicLong currentValue = new AtomicLong(0);
    
    @Override
    @Setup
    public void setup() {
        // 创建模拟的段分配器
        SegmentAllocator mockAllocator = name -> {
            long maxId = currentValue.addAndGet(SEGMENT_SIZE);
            long minId = maxId - SEGMENT_SIZE + 1;
            return new IdSegment(minId, maxId);
        };
        
        // 创建分段ID生成器实例
        idGenerator = new SegmentIdGenerator(GENERATOR_NAME, mockAllocator);
    }
    
    @Override
    @TearDown
    public void tearDown() {
        super.tearDown();
        currentValue.set(0); // 重置当前值
    }
    
    /**
     * 运行基准测试
     */
    public static void main(String[] args) throws RunnerException {
        runBenchmark(SegmentIdGeneratorBenchmark.class);
    }
} 