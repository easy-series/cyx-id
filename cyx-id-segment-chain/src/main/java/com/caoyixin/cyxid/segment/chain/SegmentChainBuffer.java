package com.caoyixin.cyxid.segment.chain;

import com.caoyixin.cyxid.core.exception.CyxIdException;
import com.caoyixin.cyxid.storage.api.IdSegment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 链式分段ID缓存器
 * 基于链表结构的ID段缓存器，提供高性能的ID生成与预取功能
 */
@Slf4j
public class SegmentChainBuffer {
    
    /**
     * ID生成器名称
     */
    @Getter
    private final String name;
    
    /**
     * 链表头节点（当前使用的节点）
     */
    private volatile SegmentChainNode head;
    
    /**
     * 链表尾节点（最近预取的节点）
     */
    private volatile SegmentChainNode tail;
    
    /**
     * 是否初始化完成
     */
    @Getter
    @Setter
    private volatile boolean initialized = false;
    
    /**
     * 安全距离百分比，当消耗到此百分比时开始预取下一个ID段
     */
    @Getter
    @Setter
    private volatile int safeDistancePercent = 20;
    
    /**
     * 链的长度（最大预取段数）
     */
    @Getter
    @Setter
    private volatile int maxChainLength = 3;
    
    /**
     * 当前链中的段数
     */
    @Getter
    private volatile int currentChainLength = 0;
    
    /**
     * 是否正在预取
     */
    @Getter
    private volatile boolean prefetching = false;
    
    /**
     * 创建链式分段ID缓存器
     *
     * @param name 生成器名称
     */
    public SegmentChainBuffer(String name) {
        this.name = name;
    }
    
    /**
     * 初始化缓冲器，添加第一个节点
     *
     * @param segment 初始ID段
     */
    public synchronized void initialize(IdSegment segment) {
        if (initialized) {
            return;
        }
        
        head = new SegmentChainNode(segment);
        tail = head;
        currentChainLength = 1;
        initialized = true;
        
        log.info("初始化链式分段ID缓存器[{}]，初始ID段：{}", name, segment);
    }
    
    /**
     * 获取下一个ID
     *
     * @return 下一个ID
     */
    public long nextId() {
        if (!initialized) {
            throw new CyxIdException("链式分段ID缓存器[" + name + "]尚未初始化");
        }
        
        // 从当前头节点获取ID
        long id = head.nextId();
        
        // 如果获取到有效ID，直接返回
        if (id > 0) {
            return id;
        }
        
        // 获取到无效ID，表示当前节点已耗尽，尝试切换到下一个节点
        synchronized (this) {
            // 再次检查当前节点，防止在同步前已被其他线程切换
            id = head.nextId();
            if (id > 0) {
                return id;
            }
            
            // 当前节点确实用完，切换到下一个节点
            SegmentChainNode next = head.getNext();
            if (next != null) {
                log.debug("切换到下一个ID段：{}", next);
                head = next;
                currentChainLength--;
                return nextId(); // 重新尝试获取ID
            }
            
            // 没有下一个节点，链式缓冲区耗尽
            throw new CyxIdException("链式分段ID缓存器[" + name + "]中的所有ID段已耗尽");
        }
    }
    
    /**
     * 添加下一个ID段到链尾
     *
     * @param segment 新的ID段
     * @return 是否添加成功
     */
    public synchronized boolean appendSegment(IdSegment segment) {
        if (!initialized) {
            initialize(segment);
            return true;
        }
        
        // 检查是否超过最大链长
        if (currentChainLength >= maxChainLength) {
            log.debug("链式分段ID缓存器[{}]已达到最大链长：{}，不再添加ID段", name, maxChainLength);
            return false;
        }
        
        // 创建新节点并添加到链尾
        SegmentChainNode newNode = new SegmentChainNode(segment);
        tail.setNext(newNode);
        tail = newNode;
        currentChainLength++;
        
        log.debug("链式分段ID缓存器[{}]添加新ID段：{}，当前链长：{}", name, segment, currentChainLength);
        return true;
    }
    
    /**
     * 判断是否需要预取
     *
     * @return 是否需要预取
     */
    public boolean shouldPrefetch() {
        if (!initialized || prefetching) {
            return false;
        }
        
        // 如果链长未达到最大值，且当前节点接近用完，则需要预取
        return currentChainLength < maxChainLength && head.shouldPrefetchNext(safeDistancePercent);
    }
    
    /**
     * 开始预取
     *
     * @return 如果已经在预取中，返回false；否则设置预取状态并返回true
     */
    public synchronized boolean startPrefetching() {
        if (prefetching) {
            return false;
        }
        prefetching = true;
        return true;
    }
    
    /**
     * 结束预取
     */
    public synchronized void endPrefetching() {
        prefetching = false;
    }
} 