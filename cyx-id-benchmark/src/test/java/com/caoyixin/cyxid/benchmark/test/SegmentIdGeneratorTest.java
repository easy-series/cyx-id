package com.caoyixin.cyxid.benchmark.test;

import com.caoyixin.cyxid.segment.SegmentIdGenerator;
import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分段ID生成器测试
 */
@DisplayName("分段ID生成器测试")
public class SegmentIdGeneratorTest {
    
    private SegmentIdGenerator idGenerator;
    private AtomicLong currentValue;
    private static final String GENERATOR_NAME = "test-segment";
    private static final int SEGMENT_SIZE = 100;
    
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
        
        idGenerator = new SegmentIdGenerator(GENERATOR_NAME, mockAllocator);
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
        int count = 200; // 跨多个段
        Set<Long> idSet = new HashSet<>(count);
        long lastId = 0;
        
        for (int i = 0; i < count; i++) {
            long id = idGenerator.generate();
            assertTrue(id > lastId, "ID应该是递增的");
            assertTrue(idSet.add(id), "ID应该是唯一的");
            lastId = id;
        }
        
        assertEquals(count, idSet.size(), "应该生成" + count + "个唯一的ID");
        
        // 验证使用了正确数量的段
        int expectedSegments = (count + SEGMENT_SIZE - 1) / SEGMENT_SIZE;
        assertEquals(expectedSegments * SEGMENT_SIZE, currentValue.get(), "应该分配正确数量的段");
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
        
        // 验证使用了正确数量的段
        int expectedSegments = (batchSize + SEGMENT_SIZE - 1) / SEGMENT_SIZE;
        assertEquals(expectedSegments * SEGMENT_SIZE, currentValue.get(), "应该分配正确数量的段");
    }
    
    @Test
    @DisplayName("生成器名称正确")
    void getNameShouldReturnCorrectName() {
        assertEquals(GENERATOR_NAME, idGenerator.getName(), "生成器名称应该正确");
        assertEquals("segment", idGenerator.getType(), "生成器类型应该是segment");
    }
} 