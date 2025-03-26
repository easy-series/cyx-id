# CyxID Snowflake Module

## 概述

CyxID Snowflake模块提供基于Twitter Snowflake算法的分布式ID生成实现。雪花算法是一种高效的分布式ID生成方案，能够生成趋势递增、全局唯一的64位长整型ID。

## 主要特性

- **高性能**：单机每秒可生成约500万个ID
- **递增趋势**：生成的ID总体呈递增趋势，满足大多数数据库索引优化要求
- **自定义结构**：支持自定义时间戳位数、机器ID位数和序列号位数
- **时钟回拨处理**：内置多种时钟回拨处理策略，保障系统稳定性
- **灵活的机器ID分配**：支持静态配置和动态分配两种机器ID分配方式

## 核心组件

### 生成器

- **SnowflakeIdGenerator**：雪花算法ID生成器实现，提供高性能的ID生成能力

### 机器ID分配

- **WorkerIdAssigner**：机器ID分配器接口
- **StaticWorkerIdAssigner**：静态机器ID分配器，通过配置指定固定的机器ID

### 时钟回拨处理

- **ClockBackwardsHandler**：时钟回拨处理器接口
- **DefaultClockBackwardsHandler**：默认时钟回拨处理实现，支持多种处理策略

### ID解析

- **SnowflakeIdParser**：雪花ID解析器，可将生成的ID解析为各个组成部分

## 雪花算法ID结构

默认的雪花ID结构如下（64位）：

```
+------+----------------------+----------------+-----------------+
| 符号位 |     时间戳部分        |     机器ID部分    |     序列号部分     |
| 1bit |       41bits        |     10bits     |     12bits     |
+------+----------------------+----------------+-----------------+
```

- **符号位**：始终为0，保证生成的ID为正数
- **时间戳部分**：41位，以毫秒为单位，可使用约69年
- **机器ID部分**：10位，最多支持1024个工作节点
- **序列号部分**：12位，同一毫秒内可生成4096个不同的ID

## 使用示例

### 基本使用

```java
// 创建静态机器ID分配器
WorkerIdAssigner workerIdAssigner = new StaticWorkerIdAssigner(1);

// 创建默认的时钟回拨处理器
ClockBackwardsHandler clockBackwardsHandler = new DefaultClockBackwardsHandler();

// 创建雪花ID生成器
SnowflakeIdGenerator generator = new SnowflakeIdGenerator(workerIdAssigner, clockBackwardsHandler);

// 生成ID
long id = generator.generate();

// 批量生成ID
List<Long> ids = generator.batchGenerate(100);

// 解析ID
SnowflakeIdParser parser = new SnowflakeIdParser();
SnowflakeIdParser.SnowflakeIdInfo idInfo = parser.parse(id);
System.out.println("Timestamp: " + idInfo.getTimestamp());
System.out.println("Worker ID: " + idInfo.getWorkerId());
System.out.println("Sequence: " + idInfo.getSequence());
```

### 自定义配置

```java
// 自定义位数配置
SnowflakeIdGenerator generator = new SnowflakeIdGenerator.Builder()
    .workerIdAssigner(workerIdAssigner)
    .clockBackwardsHandler(clockBackwardsHandler)
    .timestampBits(40)    // 自定义时间戳位数
    .workerIdBits(12)     // 自定义机器ID位数
    .sequenceBits(11)     // 自定义序列号位数
    .build();
```

## 时钟回拨处理策略

模块提供了多种时钟回拨处理策略：

- **SPIN**：自旋等待，直到时钟追上最后一次时间戳
- **EXCEPTION**：抛出异常，由上层应用处理
- **TOLERANCE**：容忍一定范围内的时钟回拨，使用序列号弥补
- **DELAY**：延迟执行，等待指定时间后重试

## 依赖关系

本模块依赖于：

- cyx-id-core：核心接口定义
- SLF4J：日志门面
- Lombok：减少样板代码 