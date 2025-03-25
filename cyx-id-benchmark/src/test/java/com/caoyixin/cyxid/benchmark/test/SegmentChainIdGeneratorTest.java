package com.caoyixin.cyxid.benchmark.test;

import com.caoyixin.cyxid.segment.chain.SegmentChainIdGenerator;
import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 链式分段ID生成器测试
 */
@DisplayName("链式分段ID生成器测试")
public class SegmentChainIdGeneratorTest {
    
    private SegmentChainIdGenerator idGenerator;
    private AtomicLong currentValue;
    private static final String GENERATOR_NAME = "test-segment-chain";
    private static final int SEGMENT_SIZE = 100;
    private static final int MAX_CHAIN_LENGTH = 3;
    private static final int PREFETCH_PERIOD = 50; // 毫秒
    
    @BeforeEach
    void setUp() {
        currentValue = new AtomicLong(0);
        
        // 创建模拟的段分配器
        SegmentAllocator mockAllocator = name -> {
            assertEquals(GENERATOR_NAME, name, "分配请求的名称应该正确");
            long maxId = currentValue.addAndGet(SEGMENT_SIZE);
            long minId = maxId - SEGMENT_SIZE + 1;
            return new IdSegment(minId, maxId);
        };
        
        idGenerator = new SegmentChainIdGenerator(
                GENERATOR_NAME, 
                mockAllocator,
                PREFETCH_PERIOD,
                20,  // 安全距离百分比
                MAX_CHAIN_LENGTH
        );
    }
    
    @AfterEach
    void tearDown() {
        if (idGenerator != null) {
            idGenerator.shutdown();
        }
    }
    
    @Test
    @DisplayName("生成ID不为负数")
    void generateShouldReturnPositiveId() {
        long id = idGenerator.generate();
        assertTrue(id > 0, "生成的ID应该是正数");
    }
    
    @Test
    @DisplayName("连续生成的ID是唯一且递增的")
    void generateShouldReturnUniqueAndAscendingIds() {
        int count = 300; // 跨多个段
        Set<Long> idSet = new HashSet<>(count);
        long lastId = 0;
        
        for (int i = 0; i < count; i++) {
            long id = idGenerator.generate();
            assertTrue(id > lastId, "ID应该是递增的");
            assertTrue(idSet.add(id), "ID应该是唯一的");
            lastId = id;
        }
        
        assertEquals(count, idSet.size(), "应该生成" + count + "个唯一的ID");
    }
    
    @Test
    @DisplayName("批量生成ID正确工作")
    void batchGenerateShouldWork() {
        int batchSize = 150; // 跨多个段
        List<Long> ids = idGenerator.batchGenerate(batchSize);
        
        assertEquals(batchSize, ids.size(), "应该生成指定数量的ID");
        
        // 检查唯一性
        Set<Long> idSet = new HashSet<>(ids);
        assertEquals(batchSize, idSet.size(), "批量生成的ID应该是唯一的");
        
        // 检查顺序
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(ids.get(i) > ids.get(i - 1), "ID应该是递增的");
        }
    }
    
    @Test
    @DisplayName("多线程环境下ID是唯一的")
    void shouldGenerateUniqueIdsInMultithreadedEnvironment() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> ids = ConcurrentHashMap.newKeySet(threadCount * idsPerThread);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        long id = idGenerator.generate();
                        assertTrue(ids.add(id), "ID应该是唯一的");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount * idsPerThread, ids.size(), "应该生成" + (threadCount * idsPerThread) + "个唯一的ID");
    }
    
    @Test
    @DisplayName("生成器名称和类型正确")
    void getNameShouldReturnCorrectName() {
        assertEquals(GENERATOR_NAME, idGenerator.getName(), "生成器名称应该正确");
        assertEquals("segment-chain", idGenerator.getType(), "生成器类型应该是segment-chain");
    }
    
    @Test
    @DisplayName("等待足够时间应触发预取机制")
    void shouldPrefetchSegmentsAfterWaiting() throws InterruptedException {
        // 先消耗一些ID
        for (int i = 0; i < 30; i++) {
            idGenerator.generate();
        }
        
        // 等待足够的时间让预取机制工作
        Thread.sleep(PREFETCH_PERIOD * 3);
        
        // 检查是否已分配足够的段
        assertTrue(currentValue.get() >= SEGMENT_SIZE * 2, "预取机制应该已请求更多段");
    }
} 