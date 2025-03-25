package com.caoyixin.cyxid.core.exception;

/**
 * CyxId框架基础异常类
 */
public class CyxIdException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public CyxIdException(String message) {
        super(message);
    }
    
    public CyxIdException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CyxIdException(Throwable cause) {
        super(cause);
    }
} 