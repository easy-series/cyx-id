package com.caoyixin.cyxid.snowflake;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.exception.IdGenerateException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法ID生成器
 * 64位ID结构：
 * - 符号位（1位）：始终为0
 * - 时间戳（41位）：毫秒级时间戳，支持约69年
 * - 工作节点ID（10位）：最多支持1024个节点
 * - 序列号（12位）：每毫秒可生成4096个ID
 */
@Slf4j
public class SnowflakeIdGenerator implements IdGenerator {
    
    // 基准时间戳（2024-01-01 00:00:00）
    private final long epochTimestamp;
    
    // 工作节点ID
    private final long workerId;
    
    // 工作节点ID位数
    private final long workerIdBits;
    
    // 序列号位数
    private final long sequenceBits;
    
    // 最大工作节点ID
    private final long maxWorkerId;
    
    // 最大序列号
    private final long maxSequence;
    
    // 工作节点ID左移位数
    private final long workerIdShift;
    
    // 时间戳左移位数
    private final long timestampShift;
    
    // 序列号，初始值为0
    private final AtomicLong sequence = new AtomicLong(0);
    
    // 上次生成ID的时间戳
    private volatile long lastTimestamp = -1L;
    
    // 时钟回拨处理器
    private final ClockBackwardsHandler clockBackwardsHandler;
    
    /**
     * 创建雪花算法ID生成器
     *
     * @param workerId 工作节点ID
     * @param epochTimestamp 基准时间戳
     * @param workerIdBits 工作节点ID位数
     * @param sequenceBits 序列号位数
     * @param clockBackwardsHandler 时钟回拨处理器
     */
    public SnowflakeIdGenerator(long workerId, long epochTimestamp, int workerIdBits, int sequenceBits, 
                               ClockBackwardsHandler clockBackwardsHandler) {
        // 参数校验
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);
        this.workerIdShift = sequenceBits;
        this.timestampShift = sequenceBits + workerIdBits;
        this.epochTimestamp = epochTimestamp;
        
        // 校验工作节点ID
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException(
                    String.format("工作节点ID必须在0-%d的范围内", maxWorkerId));
        }
        
        this.workerId = workerId;
        this.clockBackwardsHandler = clockBackwardsHandler != null ? clockBackwardsHandler : 
                new DefaultClockBackwardsHandler();
        
        log.info("初始化雪花算法ID生成器：workerId={}, epochTimestamp={}, workerBits={}, sequenceBits={}", 
                workerId, epochTimestamp, workerIdBits, sequenceBits);
    }
    
    /**
     * 使用默认参数创建雪花算法ID生成器
     * 默认使用10位工作节点ID和12位序列号
     *
     * @param workerId 工作节点ID
     */
    public SnowflakeIdGenerator(long workerId) {
        this(workerId, 1704038400000L, 10, 12, new DefaultClockBackwardsHandler());
    }
    
    /**
     * 自动分配工作节点ID
     * 
     * @param workerIdAssigner 工作节点ID分配器
     */
    public SnowflakeIdGenerator(WorkerIdAssigner workerIdAssigner) {
        this(workerIdAssigner.assignWorkerId());
    }
    
    @Override
    public synchronized long nextId() {
        long currentTimestamp = getTimestamp();
        
        // 处理时钟回拨
        if (currentTimestamp < lastTimestamp) {
            currentTimestamp = clockBackwardsHandler.handleBackwards(lastTimestamp, currentTimestamp);
        }
        
        // 如果是同一毫秒内，增加序列号
        if (currentTimestamp == lastTimestamp) {
            long nextSequence = (sequence.incrementAndGet()) & maxSequence;
            if (nextSequence == 0) {
                // 序列号用完，等待下一毫秒
                currentTimestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，重置序列号
            sequence.set(0);
        }
        
        lastTimestamp = currentTimestamp;
        
        // 计算ID
        return ((currentTimestamp - epochTimestamp) << timestampShift) | 
                (workerId << workerIdShift) | 
                sequence.get();
    }
    
    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳（毫秒）
     */
    protected long getTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上一毫秒
     * @return 下一毫秒时间戳
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getTimestamp();
        }
        return timestamp;
    }
    
    @Override
    public String getType() {
        return "snowflake";
    }
} 