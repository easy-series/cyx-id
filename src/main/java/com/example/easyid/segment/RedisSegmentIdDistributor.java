package com.example.easyid.segment;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis的号段分发器实现
 */
public class RedisSegmentIdDistributor implements SegmentIdDistributor {
    
    private static final String SEGMENT_ID_KEY_PREFIX = "easy-id:segment:";
    
    private final StringRedisTemplate redisTemplate;
    private final String name;
    private final long offset;
    private final int step;
    
    /**
     * 创建Redis号段分发器
     *
     * @param redisTemplate Redis模板
     * @param name 号段名称
     * @param offset 起始偏移量
     * @param step 步长
     */
    public RedisSegmentIdDistributor(StringRedisTemplate redisTemplate, String name, long offset, int step) {
        this.redisTemplate = redisTemplate;
        this.name = name;
        this.offset = offset;
        this.step = step;
    }
    
    /**
     * 创建Redis号段分发器，使用默认的偏移量和步长
     *
     * @param redisTemplate Redis模板
     * @param name 号段名称
     */
    public RedisSegmentIdDistributor(StringRedisTemplate redisTemplate, String name) {
        this(redisTemplate, name, 0, 100);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public IdSegment nextSegment() {
        String key = getSegmentKey();
        
        // 使用Redis的原子操作递增并获取当前值
        Long value = redisTemplate.opsForValue().increment(key, step);
        
        if (value == null) {
            throw new IllegalStateException("无法从Redis获取ID号段值");
        }
        
        // 计算号段起始值
        long minId = value - step + offset;
        
        return new IdSegment(minId, step);
    }
    
    @Override
    public long getOffset() {
        return offset;
    }
    
    @Override
    public int getStep() {
        return step;
    }
    
    /**
     * 获取Redis中存储号段值的键
     *
     * @return Redis键名
     */
    private String getSegmentKey() {
        return SEGMENT_ID_KEY_PREFIX + name;
    }
} 