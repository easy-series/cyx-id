# CyxID Storage API Module

## 概述

CyxID Storage API模块定义了存储层的抽象接口，为分布式ID生成提供统一的持久化服务规范。该模块作为CyxID框架的存储层抽象，使ID生成器能够与不同的存储实现解耦，提供了良好的扩展性。

## 主要功能

- **存储抽象层**：定义统一的存储接口，实现ID生成和存储的解耦
- **号段分配接口**：支持号段模式ID生成器获取和管理ID段
- **机器ID分配接口**：支持雪花算法中的工作节点ID分配和管理
- **数据模型定义**：提供ID段、机器状态等核心数据模型

## 核心接口

### 号段管理接口

- **SegmentAllocator**：号段分配器接口，负责获取和管理ID段
- **IdSegment**：号段数据模型，包含号段的起始值、步长等信息

### 机器ID管理接口

- **MachineIdAllocator**：机器ID分配器接口，负责为雪花算法分配唯一的工作节点ID
- **MachineIdStorage**：机器ID存储接口，负责持久化工作节点信息
- **MachineState**：机器状态数据模型，记录节点的活跃状态和心跳信息

## 接口说明

### SegmentAllocator

```java
public interface SegmentAllocator {
    
    /**
     * 分配一个新的ID段
     *
     * @param key 业务键名
     * @return 分配的ID段信息
     */
    IdSegment allocate(String key);
}
```

### MachineIdAllocator

```java
public interface MachineIdAllocator {
    
    /**
     * 分配机器ID
     *
     * @param groupName 组名
     * @return 分配的机器ID
     */
    long allocate(String groupName);
    
    /**
     * 释放机器ID
     *
     * @param groupName 组名
     * @param machineId 机器ID
     */
    void release(String groupName, long machineId);
}
```

### MachineIdStorage

```java
public interface MachineIdStorage {
    
    /**
     * 保存机器状态
     *
     * @param groupName 组名
     * @param machineState 机器状态
     */
    void save(String groupName, MachineState machineState);
    
    /**
     * 查询机器状态
     *
     * @param groupName 组名
     * @param machineId 机器ID
     * @return 机器状态
     */
    MachineState query(String groupName, long machineId);
}
```

## 数据模型

### IdSegment

ID号段模型，包含以下主要属性：

- **key**：业务键名，用于区分不同业务的ID段
- **startId**：号段起始ID
- **step**：号段步长，表示该号段包含的ID数量
- **maxId**：号段最大ID (startId + step - 1)
- **timestamp**：分配时间戳，用于乐观锁控制和监控

### MachineState

机器状态模型，包含以下主要属性：

- **machineId**：机器ID
- **lastHeartbeat**：最后心跳时间
- **hostName**：主机名
- **ipAddress**：IP地址
- **processId**：进程ID
- **activeStatus**：活跃状态

## 实现扩展

要实现自定义存储，只需实现相应的接口：

```java
// 自定义号段分配器
public class CustomSegmentAllocator implements SegmentAllocator {
    @Override
    public IdSegment allocate(String key) {
        // 自定义号段分配逻辑
        return yourAllocationLogic(key);
    }
}

// 自定义机器ID分配器
public class CustomMachineIdAllocator implements MachineIdAllocator {
    @Override
    public long allocate(String groupName) {
        // 自定义机器ID分配逻辑
        return yourAllocationLogic(groupName);
    }
    
    @Override
    public void release(String groupName, long machineId) {
        // 自定义机器ID释放逻辑
        yourReleaseLogic(groupName, machineId);
    }
}
```

## 存储实现推荐

CyxID框架提供了以下存储实现：

- **Redis实现**：基于Redis的高性能分布式存储实现
- **数据库实现**：基于关系型数据库的持久化存储实现（待开发）
- **Zookeeper实现**：基于Zookeeper的高可靠分布式协调实现（待开发）

## 依赖关系

本模块作为接口层，具有最少的依赖：

- cyx-id-core：核心异常定义
- Lombok：减少样板代码 