package com.example.easyid.examples;

import java.util.Scanner;

/**
 * Easy-ID示例运行器
 * 可以选择运行不同的示例
 */
public class EasyIdExamples {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            showMenu();
            
            System.out.print("\n请选择要运行的示例 (输入0退出): ");
            int choice = -1;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                scanner.nextLine(); // 清除输入
                System.out.println("输入无效，请重新输入数字选项。");
                continue;
            }
            
            switch (choice) {
                case 0:
                    System.out.println("退出程序");
                    return;
                case 1:
                    System.out.println("\n运行雪花算法示例...\n");
                    SnowflakeIdExample.main(args);
                    break;
                case 2:
                    System.out.println("\n运行段ID生成器示例...\n");
                    SegmentIdExample.main(args);
                    break;
                case 3:
                    System.out.println("\n运行链式段ID生成器示例...\n");
                    SegmentChainIdExample.main(args);
                    break;
                default:
                    System.out.println("输入无效，请重新选择。");
                    break;
            }
            
            System.out.println("\n按Enter键继续...");
            scanner.nextLine(); // 消耗换行符
            scanner.nextLine(); // 等待用户按下Enter
        }
    }
    
    private static void showMenu() {
        System.out.println("\n======== Easy-ID 示例 ========");
        System.out.println("1. 雪花算法示例");
        System.out.println("2. 段ID生成器示例");
        System.out.println("3. 链式段ID生成器示例");
        System.out.println("0. 退出");
        System.out.println("============================");
    }
} 