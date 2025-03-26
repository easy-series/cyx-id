# CyxID Segment Chain Module

## 概述

CyxID Segment Chain模块提供基于链式号段模式的分布式ID生成实现。这是对传统号段模式的创新性改进，通过链表结构管理多个号段和无锁设计，实现了极高的并发性能，特别适合多线程高QPS环境。

## 主要特性

- **超高性能**：单机每秒可生成约450万个ID，多线程下可达3000万QPS
- **无锁设计**：采用链式结构和CAS操作，极大减少线程竞争
- **预取机制**：后台线程自动预取和管理号段，最大化ID生成性能
- **动态调整**：根据系统负载自动调整预取策略，优化资源使用
- **平滑降级**：存储层故障时保持服务可用，系统恢复后自动同步状态

## 核心组件

### 生成器

- **SegmentChainIdGenerator**：链式号段ID生成器，提供高性能的ID生成能力
- **SegmentChainIdGeneratorFactory**：链式号段ID生成器工厂，创建和配置生成器实例

### 链式管理

- **SegmentChainNode**：号段链表节点，代表一个ID号段
- **SegmentChainBuffer**：号段链表缓冲区，管理整个链表结构
- **SegmentChainBufferManager**：链表缓冲区管理器，协调多线程访问

### 预取机制

- **PrefetchWorker**：后台预取工作线程，负责异步加载和维护号段链表

## 工作原理

链式号段模式的工作原理：

1. 使用链表结构维护多个连续的ID号段
2. 后台线程持续预取号段并追加到链表尾部
3. ID生成通过CAS操作从头部节点获取ID，无需加锁
4. 头部节点用尽后自动移除，后续节点成为新的头部
5. 链表长度动态调整，保持适当的预取量，平衡性能和资源消耗

与传统号段模式的区别：
- 传统号段模式：使用双Buffer机制，最多预加载一个备用号段
- 链式号段模式：使用链表结构，可预加载多个号段，且采用无锁设计

## 使用示例

### 基本使用

```java
// 创建号段分配器
SegmentAllocator segmentAllocator = new YourSegmentAllocator();

// 创建链式号段ID生成器
SegmentChainIdGenerator generator = new SegmentChainIdGenerator.Builder()
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
// 自定义配置的链式号段ID生成器
SegmentChainIdGenerator generator = new SegmentChainIdGenerator.Builder()
    .name("order")
    .allocator(segmentAllocator)
    .step(1000)                // 设置号段步长
    .initialCapacity(5)        // 设置链表初始容量
    .prefetchThreshold(3)      // 设置预取阈值
    .maxCapacity(10)           // 设置链表最大容量
    .prefetchPeriod(500)       // 设置预取周期（毫秒）
    .scheduledThreads(2)       // 设置预取线程数
    .build();
```

## 性能优化建议

1. **调整链表容量**：
   - 根据系统QPS设置合理的链表长度，高QPS系统可增加容量
   - 避免设置过大容量，以减少不必要的资源消耗和ID浪费

2. **优化预取策略**：
   - 根据业务峰值调整预取阈值和周期
   - 高并发系统可增加预取线程数

3. **号段步长设置**：
   - 高并发系统建议设置较大步长，减少分配请求频率
   - 考虑系统重启时可接受的ID浪费率

## 适用场景

- **超高并发系统**：秒杀、交易等高峰场景
- **多线程批量处理**：批量导入、数据处理等场景
- **混合读写负载**：既有高并发读又有写操作的场景

## 注意事项

- **内存占用**：链式结构会比传统号段模式占用更多内存
- **预取线程资源**：后台预取线程会占用系统资源，建议根据系统情况合理配置
- **ID连续性**：虽然ID总体递增，但多线程下可能不严格连续

## 依赖关系

本模块依赖于：

- cyx-id-core：核心接口定义
- cyx-id-storage-api：存储层抽象接口
- cyx-id-segment：基础号段模型和部分共享逻辑
- SLF4J：日志门面
- Lombok：减少样板代码 