package com.example.easyid.snowflake;

import com.example.easyid.core.Clock;
import com.example.easyid.core.SystemClock;
import com.example.easyid.machine.MachineIdDistributor;
import com.example.easyid.machine.ManualMachineIdDistributor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 默认的雪花算法ID生成器实现
 * <pre>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 0000000000 - 000000000000
 * │   │                                          │   │            │
 * └─┬─┘                                          │   │            └──────────── 序列号部分(12位)
 *   │                                            │   └─────────────── 机器号部分(10位)
 *   └────────────────────────────────────────────┴────────────── 时间戳部分(41位)
 * </pre>
 */
public class DefaultSnowflakeId implements SnowflakeId {
    
    /**
     * 默认的ID名称
     */
    public static final String DEFAULT_NAME = "snowflake";
    
    /**
     * 时间戳位数：41位，可用69年
     */
    private static final int DEFAULT_TIMESTAMP_BITS = 41;
    
    /**
     * 机器号位数：10位，最多支持1024台机器
     */
    private static final int DEFAULT_MACHINE_BITS = 10;
    
    /**
     * 序列号位数：12位，每毫秒可产生4096个ID
     */
    private static final int DEFAULT_SEQUENCE_BITS = 12;
    
    private final String name;
    private final Clock clock;
    private final int machineId;
    private final int timestampBits;
    private final int machineBits;
    private final int sequenceBits;
    
    /**
     * 时间戳左移位数 = 机器号位数 + 序列号位数
     */
    private final int timestampLeftShift;
    
    /**
     * 机器号左移位数 = 序列号位数
     */
    private final int machineLeftShift;
    
    /**
     * 序列号掩码，用来限定序列号的最大值
     */
    private final long sequenceMask;
    
    /**
     * 最后一次的时间戳
     */
    private volatile long lastTimestamp = -1L;
    
    /**
     * 当前毫秒内的序列号
     */
    private volatile long sequence = 0L;
    
    /**
     * 使用默认参数创建雪花算法ID生成器
     */
    public DefaultSnowflakeId() {
        this(DEFAULT_NAME, new SystemClock(), 
             new ManualMachineIdDistributor("default", 0).distribute(1 << DEFAULT_MACHINE_BITS),
             DEFAULT_TIMESTAMP_BITS, DEFAULT_MACHINE_BITS, DEFAULT_SEQUENCE_BITS);
    }
    
    /**
     * 创建雪花算法ID生成器
     *
     * @param name ID生成器名称
     * @param clock 时钟实例
     * @param machineId 机器号
     * @param timestampBits 时间戳位数
     * @param machineBits 机器号位数
     * @param sequenceBits 序列号位数
     */
    public DefaultSnowflakeId(String name, Clock clock, int machineId, 
                             int timestampBits, int machineBits, int sequenceBits) {
        this.name = name;
        this.clock = clock;
        this.machineId = machineId;
        this.timestampBits = timestampBits;
        this.machineBits = machineBits;
        this.sequenceBits = sequenceBits;
        
        // 检查机器号是否超出范围
        int maxMachineId = ~(-1 << machineBits);
        if (machineId > maxMachineId) {
            throw new IllegalArgumentException(
                    String.format("机器号[%d]超出最大范围[%d]", machineId, maxMachineId));
        }
        
        this.timestampLeftShift = machineBits + sequenceBits;
        this.machineLeftShift = sequenceBits;
        this.sequenceMask = ~(-1L << sequenceBits);
    }
    
    @Override
    public synchronized long nextId() {
        long currentTimestamp = clock.getTimestamp();
        
        // 检查时钟回拨
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException(
                    String.format("时钟回拨，拒绝生成ID。上次时间戳: %d, 当前时间戳: %d", 
                            lastTimestamp, currentTimestamp));
        }
        
        // 如果是同一毫秒内，则增加序列号
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 如果序列号用完，则获取下一毫秒的时间戳
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 如果不是同一毫秒，则重置序列号
            sequence = 0L;
        }
        
        // 更新最后时间戳
        lastTimestamp = currentTimestamp;
        
        // 生成ID
        return (currentTimestamp << timestampLeftShift) | 
               (machineId << machineLeftShift) | 
               sequence;
    }
    
    /**
     * 等待到下一毫秒
     *
     * @param lastTimestamp 上次的时间戳
     * @return 下一毫秒的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = clock.getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = clock.getTimestamp();
        }
        return timestamp;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getTimestampBits() {
        return timestampBits;
    }
    
    @Override
    public int getMachineBits() {
        return machineBits;
    }
    
    @Override
    public int getSequenceBits() {
        return sequenceBits;
    }
    
    @Override
    public int getMachineId() {
        return machineId;
    }
    
    @Override
    public String parseId(long id) {
        long timestampValue = (id >> timestampLeftShift);
        long machineValue = (id >> machineLeftShift) & ~(-1L << machineBits);
        long sequenceValue = id & sequenceMask;
        
        Date date = new Date(timestampValue + clock.getEpoch());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
        return String.format("{timestamp: %s, machineId: %d, sequence: %d}",
                sdf.format(date), machineValue, sequenceValue);
    }
    
    @Override
    public String convertId(long id) {
        // 格式化为更友好的字符串
        return String.valueOf(id);
    }
} 