package com.caoyixin.cyxid.benchmark.core;

import com.caoyixin.cyxid.core.IdGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * ID生成器基准测试基类
 * 提供基础测试方法和工具
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
public abstract class IdGeneratorBenchmark {
    
    /**
     * 要测试的ID生成器
     */
    protected IdGenerator idGenerator;
    
    /**
     * 准备测试环境
     * 子类需要实现此方法，初始化ID生成器
     */
    @Setup
    public abstract void setup();
    
    /**
     * 清理测试环境
     * 子类可以选择重写此方法，释放资源
     */
    @TearDown
    public void tearDown() {
        // 默认实现为空，子类可以根据需要重写
    }
    
    /**
     * 测试单个ID生成性能
     */
    @Benchmark
    public void benchmarkSingleId(Blackhole blackhole) {
        blackhole.consume(idGenerator.generate());
    }
    
    /**
     * 测试批量生成ID性能（10个）
     */
    @Benchmark
    public void benchmarkBatchId10(Blackhole blackhole) {
        blackhole.consume(idGenerator.batchGenerate(10));
    }
    
    /**
     * 测试批量生成ID性能（100个）
     */
    @Benchmark
    public void benchmarkBatchId100(Blackhole blackhole) {
        blackhole.consume(idGenerator.batchGenerate(100));
    }
    
    /**
     * 测试多线程环境下的性能
     * 使用JMH的线程数参数
     */
    @Benchmark
    @Threads(8)
    public void benchmarkWithThreads(Blackhole blackhole) {
        blackhole.consume(idGenerator.generate());
    }
    
    /**
     * 运行基准测试的主方法
     * 
     * @param benchmarkClass 基准测试类
     * @throws RunnerException 运行异常
     */
    protected static void runBenchmark(Class<?> benchmarkClass) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(benchmarkClass.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-" + benchmarkClass.getSimpleName() + ".json")
                .build();
        
        new Runner(options).run();
    }
} 