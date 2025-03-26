# CyxID Core Module

## 概述

CyxID Core模块是整个CyxID框架的核心基础组件，提供了ID生成的核心接口定义和异常处理机制。所有其他模块都依赖于这个核心模块，它定义了统一的ID生成器规范和API。

## 主要功能

- **核心接口定义**：提供ID生成的标准接口`IdGenerator`
- **ID生成器提供者模式**：通过`IdGeneratorProvider`接口实现ID生成器的管理和获取
- **标准异常体系**：定义了框架中使用的各类异常，如时钟回拨异常、机器ID分配异常等

## 核心组件

### 接口

- **IdGenerator**：所有ID生成器必须实现的核心接口，定义了生成单个ID和批量生成ID的方法
- **IdGeneratorProvider**：ID生成器提供者接口，管理和获取不同的ID生成器实例

### 异常类

- **CyxIdException**：框架基础异常类，所有特定异常都继承自此类
- **ClockBackwardsException**：时钟回拨异常，雪花算法中使用
- **MachineIdAllocationException**：机器ID分配异常

## 使用示例

### 基本使用

```java
// 获取ID生成器提供者
IdGeneratorProvider provider = new DefaultIdGeneratorProvider();

// 注册一个ID生成器
IdGenerator generator = new YourIdGenerator();
provider.registerGenerator("orderIdGenerator", generator);

// 获取并使用ID生成器
IdGenerator orderIdGen = provider.getGenerator("orderIdGenerator");
long id = orderIdGen.generate();

// 批量生成ID
List<Long> ids = orderIdGen.batchGenerate(100);
```

## 扩展点

如果需要实现自己的ID生成器，只需实现`IdGenerator`接口：

```java
public class CustomIdGenerator implements IdGenerator {
    
    @Override
    public long generate() {
        // 自定义ID生成逻辑
        return yourGenerationLogic();
    }
    
    @Override
    public String getName() {
        return "custom-generator";
    }
    
    @Override
    public String getType() {
        return "CUSTOM";
    }
}
```

## 依赖关系

本模块作为核心模块，被框架中的其他所有模块依赖。它自身只依赖以下外部库：

- Lombok：用于减少样板代码
- SLF4J：日志门面框架
- JUnit：用于单元测试 