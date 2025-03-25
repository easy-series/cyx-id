package com.caoyixin.cyxid.segment;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分段ID生成器工厂
 * 用于创建和管理多个分段ID生成器实例
 */
@Slf4j
public class SegmentIdGeneratorFactory {
    
    /**
     * 生成器缓存，按名称索引
     */
    private final Map<String, SegmentIdGenerator> generatorCache = new ConcurrentHashMap<>();
    
    /**
     * ID段分配器
     */
    private final SegmentAllocator segmentAllocator;
    
    /**
     * 创建分段ID生成器工厂
     *
     * @param segmentAllocator ID段分配器
     */
    public SegmentIdGeneratorFactory(SegmentAllocator segmentAllocator) {
        this.segmentAllocator = segmentAllocator;
    }
    
    /**
     * 获取或创建指定名称的分段ID生成器
     *
     * @param name 生成器名称
     * @return 分段ID生成器
     */
    public IdGenerator getGenerator(String name) {
        return generatorCache.computeIfAbsent(name, this::createGenerator);
    }
    
    /**
     * 创建新的分段ID生成器
     *
     * @param name 生成器名称
     * @return 新创建的分段ID生成器
     */
    private SegmentIdGenerator createGenerator(String name) {
        log.info("创建分段ID生成器：{}", name);
        return new SegmentIdGenerator(name, segmentAllocator);
    }
    
    /**
     * 关闭工厂，释放资源
     */
    public void shutdown() {
        generatorCache.clear();
    }
} 