package com.caoyixin.cyxid.core.exception;

/**
 * 机器ID分配异常
 */
public class MachineIdAllocationException extends CyxIdException {
    
    private static final long serialVersionUID = 1L;
    
    public MachineIdAllocationException(String message) {
        super(message);
    }
    
    public MachineIdAllocationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MachineIdAllocationException(Throwable cause) {
        super(cause);
    }
} 