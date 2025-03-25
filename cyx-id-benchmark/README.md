# CyxID 基准测试模块

本模块提供用于测试CyxID框架中各种ID生成器性能和正确性的工具。

## 功能

本模块包含以下主要功能：

1. **基准测试**：使用JMH（Java Microbenchmark Harness）框架对各种ID生成器进行性能测试
2. **单元测试**：验证各种ID生成器的正确性
3. **综合比较**：对不同ID生成器进行性能比较

## 基准测试

### 可用的基准测试

- **SnowflakeIdGeneratorBenchmark**：雪花算法ID生成器基准测试
- **SegmentIdGeneratorBenchmark**：分段ID生成器基准测试
- **SegmentChainIdGeneratorBenchmark**：链式分段ID生成器基准测试

### 运行基准测试

#### 运行所有基准测试

```bash
# 编译并运行所有基准测试
mvn clean package
java -jar target/benchmarks.jar
```

#### 运行特定基准测试

```bash
# 只运行雪花算法基准测试
java -jar target/benchmarks.jar SnowflakeIdGeneratorBenchmark

# 只运行分段ID基准测试
java -jar target/benchmarks.jar SegmentIdGeneratorBenchmark

# 只运行链式分段ID基准测试
java -jar target/benchmarks.jar SegmentChainIdGeneratorBenchmark
```

#### 使用JMH参数

```bash
# 设置线程数为4，每个测试运行10次
java -jar target/benchmarks.jar -t 4 -i 10

# 生成HTML报告
java -jar target/benchmarks.jar -rf html
```

## 单元测试

### 可用的单元测试

- **SnowflakeIdGeneratorTest**：验证雪花算法ID生成器的正确性
- **SegmentIdGeneratorTest**：验证分段ID生成器的正确性
- **SegmentChainIdGeneratorTest**：验证链式分段ID生成器的正确性

### 运行单元测试

```bash
# 运行所有单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=SnowflakeIdGeneratorTest
mvn test -Dtest=SegmentIdGeneratorTest
mvn test -Dtest=SegmentChainIdGeneratorTest
```

## 基准测试结果解读

基准测试结果会输出到JSON文件中，您可以使用JMH提供的工具进行可视化查看。

主要指标说明：

- **Throughput (ops/s)**：每秒可执行的操作数，越高越好
- **Average Time (ns/op)**：每个操作的平均时间，单位纳秒，越低越好
- **p90, p95, p99**：操作时间的90%、95%、99%百分位数，反映性能稳定性

## 性能优化建议

根据基准测试结果，有以下几点优化建议：

1. **高并发场景**：推荐使用链式分段ID生成器，其无锁设计在多线程环境下性能最佳
2. **低延迟场景**：推荐使用雪花算法生成器，单次ID生成延迟最低
3. **高可靠性场景**：推荐使用分段ID生成器，可以容忍短时间的存储层故障

## 许可证

与主项目相同，采用MIT许可证。 