package com.caoyixin.cyxid.benchmark.snowflake;

import com.caoyixin.cyxid.benchmark.core.IdGeneratorBenchmark;
import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.snowflake.DefaultClockBackwardsHandler;
import com.caoyixin.cyxid.snowflake.SnowflakeIdGenerator;
import com.caoyixin.cyxid.snowflake.StaticWorkerIdAssigner;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.RunnerException;

/**
 * 雪花算法ID生成器基准测试
 */
public class SnowflakeIdGeneratorBenchmark extends IdGeneratorBenchmark {
    
    @Override
    @Setup
    public void setup() {
        // 创建雪花算法生成器实例
        idGenerator = new SnowflakeIdGenerator(
                1L, // 工作节点ID
                1672502400000L, // 自定义纪元（2023-01-01 00:00:00.000）
                10, // 工作ID比特数
                12, // 序列号比特数
                new DefaultClockBackwardsHandler() // 默认时钟回拨处理器
        );
    }
    
    /**
     * 运行基准测试
     */
    public static void main(String[] args) throws RunnerException {
        runBenchmark(SnowflakeIdGeneratorBenchmark.class);
    }
} 