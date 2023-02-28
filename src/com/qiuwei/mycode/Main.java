package com.qiuwei.mycode;


import com.spi.facade.LoggerService;

public class Main {

    public static void main(String[] args) {
        LoggerService loggerService = LoggerService.getService();
        loggerService.info("你好");
        loggerService.debug("测试Java SPI 机制");
    }
}
