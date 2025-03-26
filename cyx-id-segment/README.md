# CyxID Segment Module

## 概述

CyxID Segment模块提供基于号段模式的分布式ID生成实现。号段模式通过预先分配一段ID范围（号段）并在内存中使用，大幅减少对存储层的访问频率，是一种高性能的分布式ID生成方案。

## 主要特性

- **高性能**：单机每秒可生成约300万个ID
- **低存储压力**：预分配号段，显著减少存储层访问频率
- **双Buffer机制**：异步加载下一个号段，避免获取ID时的阻塞等待
- **严格递增**：生成的ID严格递增，适合对ID单调性有强要求的场景
- **动态步长调整**：根据系统负载动态调整号段大小，优化性能

## 核心组件

### 生成器

- **SegmentIdGenerator**：号段模式ID生成器实现，提供高性能的ID生成能力
- **SegmentIdGeneratorFactory**：号段ID生成器工厂，用于创建和配置生成器实例

### 双Buffer机制

- 通过双Buffer设计，在当前号段消耗到阈值时，异步加载下一个号段，实现无阻塞切换

## 工作原理

号段模式的工作原理：

1. 从存储服务获取一段可用的ID范围（如[1001-2000]）
2. 将这段ID缓存在内存中，分配ID时从缓存中获取
3. 当当前号段消耗到设定的阈值（如剩余20%）时，异步加载下一个号段
4. 当前号段用尽后，立即切换到下一个已加载的号段，实现无阻塞切换
5. 重复以上步骤，持续提供ID服务

## 使用示例

### 基本使用

```java
// 创建号段分配器
SegmentAllocator segmentAllocator = new YourSegmentAllocator();

// 创建号段ID生成器
SegmentIdGenerator generator = new SegmentIdGenerator.Builder()
    .name("order")  // 业务标识名称
    .allocator(segmentAllocator)
    .build();

// 初始化生成器
generator.init();

// 生成ID
long id = generator.generate();

// 批量生成ID
List<Long> ids = generator.batchGenerate(100);
```

### 自定义配置

```java
// 自定义配置的号段ID生成器
SegmentIdGenerator generator = new SegmentIdGenerator.Builder()
    .name("order")
    .allocator(segmentAllocator)
    .step(1000)             // 设置号段步长
    .loadingPercent(25)     // 设置加载下一个号段的阈值百分比
    .retryTimes(3)          // 设置加载失败时的重试次数
    .retryInterval(200)     // 设置重试间隔（毫秒）
    .build();
```

## 性能优化建议

1. **合理设置号段步长**：
   - 步长过小会增加存储层访问频率
   - 步长过大会造成ID浪费（系统重启时）
   - 建议根据业务峰值QPS和可接受的ID浪费率设置

2. **调整加载阈值**：
   - 默认为20%，可根据系统负载和网络延迟调整
   - 高负载系统可适当提高阈值，确保有足够时间加载下一号段

3. **实现高可用的存储层**：
   - 号段分配依赖存储服务，需确保存储层的高可用性
   - 可考虑为存储服务增加缓存或读写分离策略

## 适用场景

- **数据库分库分表**：生成全局唯一且严格递增的主键
- **高并发订单系统**：生成严格递增的订单号
- **任务编号系统**：需要可读性强的连续编号

## 限制因素

- **依赖外部存储**：依赖号段分配服务的可用性
- **极端情况下的阻塞风险**：如果号段用尽且下一号段加载失败，可能导致短暂阻塞
- **服务重启时的ID浪费**：当前号段未用完时服务重启，会导致号段内剩余ID浪费

## 依赖关系

本模块依赖于：

- cyx-id-core：核心接口定义
- cyx-id-storage-api：存储层抽象接口
- SLF4J：日志门面
- Lombok：减少样板代码 