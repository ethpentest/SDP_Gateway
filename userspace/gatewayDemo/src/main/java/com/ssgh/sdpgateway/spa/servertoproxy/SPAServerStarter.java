package com.ssgh.sdpgateway.spa.servertoproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SPAServerStarter implements Runnable{
    @Resource
    SPAServer spaServer;
    @Override
    public void run() {
        try {
            spaServer.start();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
