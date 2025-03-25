package com.caoyixin.cyxid.benchmark.segmentchain;

import com.caoyixin.cyxid.benchmark.core.IdGeneratorBenchmark;
import com.caoyixin.cyxid.segment.chain.SegmentChainIdGenerator;
import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 链式分段ID生成器基准测试
 */
public class SegmentChainIdGeneratorBenchmark extends IdGeneratorBenchmark {
    
    private static final String GENERATOR_NAME = "benchmark";
    private static final int SEGMENT_SIZE = 1000;
    private static final int MAX_CHAIN_LENGTH = 5;
    private static final int PREFETCH_PERIOD = 1000; // 毫秒
    private static final int SAFE_DISTANCE_PERCENT = 20;
    
    // 用于模拟ID段分配的当前值
    private final AtomicLong currentValue = new AtomicLong(0);
    
    // 保存生成器引用，用于清理资源
    private SegmentChainIdGenerator segmentChainIdGenerator;
    
    @Override
    @Setup
    public void setup() {
        // 创建模拟的段分配器
        SegmentAllocator mockAllocator = name -> {
            long maxId = currentValue.addAndGet(SEGMENT_SIZE);
            long minId = maxId - SEGMENT_SIZE + 1;
            return new IdSegment(minId, maxId);
        };
        
        // 创建链式分段ID生成器实例
        segmentChainIdGenerator = new SegmentChainIdGenerator(
                GENERATOR_NAME,
                mockAllocator,
                PREFETCH_PERIOD,
                SAFE_DISTANCE_PERCENT,
                MAX_CHAIN_LENGTH
        );
        
        idGenerator = segmentChainIdGenerator;
    }
    
    @Override
    @TearDown
    public void tearDown() {
        // 清理资源
        if (segmentChainIdGenerator != null) {
            segmentChainIdGenerator.shutdown();
        }
        currentValue.set(0); // 重置当前值
    }
    
    /**
     * 运行基准测试
     */
    public static void main(String[] args) throws RunnerException {
        runBenchmark(SegmentChainIdGeneratorBenchmark.class);
    }
} 