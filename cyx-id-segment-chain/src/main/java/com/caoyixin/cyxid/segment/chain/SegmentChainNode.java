package com.caoyixin.cyxid.segment.chain;

import com.caoyixin.cyxid.storage.api.IdSegment;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 链式分段ID节点
 * 表示链式分段ID缓存中的一个ID段节点
 */
@Getter
public class SegmentChainNode {
    
    /**
     * ID段信息
     */
    private final IdSegment segment;
    
    /**
     * 当前ID值
     */
    private final AtomicLong currentValue;
    
    /**
     * 下一个节点
     */
    private volatile SegmentChainNode next;
    
    /**
     * 创建链式分段ID节点
     *
     * @param segment ID段
     */
    public SegmentChainNode(IdSegment segment) {
        this.segment = segment;
        this.currentValue = new AtomicLong(segment.getMinId() - 1); // 初始值为最小值-1，首次自增即为最小值
        this.next = null;
    }
    
    /**
     * 设置下一个节点
     *
     * @param next 下一个节点
     */
    public void setNext(SegmentChainNode next) {
        this.next = next;
    }
    
    /**
     * 获取下一个ID
     *
     * @return 下一个ID，如果ID段已用完则返回-1
     */
    public long nextId() {
        long value = currentValue.incrementAndGet();
        if (value <= segment.getMaxId()) {
            return value;
        }
        // ID段已用完，返回-1
        return -1;
    }
    
    /**
     * 当前ID段的剩余可用数量
     *
     * @return 剩余可用数量
     */
    public long remainingIdsCount() {
        return segment.getMaxId() - currentValue.get();
    }
    
    /**
     * 是否需要预取下一个节点
     *
     * @param safeDistance 安全距离百分比
     * @return 是否需要预取
     */
    public boolean shouldPrefetchNext(int safeDistance) {
        long threshold = (long)(segment.getStep() * (safeDistance / 100.0));
        return remainingIdsCount() < threshold;
    }
    
    /**
     * 是否已耗尽当前ID段
     *
     * @return 是否已耗尽
     */
    public boolean isExhausted() {
        return currentValue.get() >= segment.getMaxId();
    }
    
    @Override
    public String toString() {
        return "SegmentChainNode{" +
                "segment=" + segment +
                ", currentValue=" + currentValue.get() +
                ", next=" + (next != null ? "exists" : "null") +
                '}';
    }
} 