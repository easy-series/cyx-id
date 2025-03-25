package com.caoyixin.cyxid.benchmark.test;

import com.caoyixin.cyxid.snowflake.DefaultClockBackwardsHandler;
import com.caoyixin.cyxid.snowflake.SnowflakeIdGenerator;
import com.caoyixin.cyxid.snowflake.SnowflakeIdParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雪花算法ID生成器测试
 */
@DisplayName("雪花算法ID生成器测试")
public class SnowflakeIdGeneratorTest {
    
    private SnowflakeIdGenerator idGenerator;
    private final long workerId = 1L;
    private final long epoch = 1672502400000L; // 2023-01-01 00:00:00.000
    
    @BeforeEach
    void setUp() {
        idGenerator = new SnowflakeIdGenerator(
                workerId,
                epoch,
                10, // 工作ID比特数
                12, // 序列号比特数
                new DefaultClockBackwardsHandler()
        );
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
        int count = 1000;
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
        int batchSize = 100;
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
    @DisplayName("解析ID可以提取正确的工作ID")
    void parseIdShouldExtractCorrectWorkerId() {
        long id = idGenerator.generate();
        
        SnowflakeIdParser.SnowflakeIdInfo info = SnowflakeIdParser.parse(
                id, epoch, 22, 12, 0x3FF, 0xFFF
        );
        
        assertEquals(workerId, info.getWorkerId(), "解析出的工作节点ID应该正确");
    }
    
    @Test
    @DisplayName("生成器名称正确")
    void getNameShouldReturnCorrectName() {
        assertEquals("snowflake", idGenerator.getType(), "生成器类型应该是snowflake");
    }
} 