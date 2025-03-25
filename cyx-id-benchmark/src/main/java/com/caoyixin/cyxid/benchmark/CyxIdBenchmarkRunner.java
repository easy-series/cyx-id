package com.caoyixin.cyxid.benchmark;

import com.caoyixin.cyxid.benchmark.segment.SegmentIdGeneratorBenchmark;
import com.caoyixin.cyxid.benchmark.segmentchain.SegmentChainIdGeneratorBenchmark;
import com.caoyixin.cyxid.benchmark.snowflake.SnowflakeIdGeneratorBenchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * CyxId基准测试运行器
 * 用于运行所有ID生成器的基准测试并生成比较报告
 */
public class CyxIdBenchmarkRunner {
    
    /**
     * 运行所有ID生成器的基准测试
     */
    public static void main(String[] args) throws RunnerException {
        System.out.println("开始运行CyxId基准测试...");
        
        // 创建测试选项
        Options options = new OptionsBuilder()
                // 包含所有基准测试类
                .include(SnowflakeIdGeneratorBenchmark.class.getSimpleName())
                .include(SegmentIdGeneratorBenchmark.class.getSimpleName())
                .include(SegmentChainIdGeneratorBenchmark.class.getSimpleName())
                // 设置输出格式为JSON
                .resultFormat(ResultFormatType.JSON)
                .result("cyx-id-benchmark-comparison.json")
                // 设置HTML报告
                .jvmArgs("-Djmh.generator.jsonreport.dir=./jmh-reports/")
                // 热身和测量配置
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(1)
                .build();
        
        // 运行基准测试
        new Runner(options).run();
        
        System.out.println("CyxId基准测试完成！");
        System.out.println("结果保存在: cyx-id-benchmark-comparison.json");
    }
} 