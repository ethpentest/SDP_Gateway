package com.ssgh.sdpgateway.mtls.servertoproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ServerStarter implements Runnable{
    @Resource
    Server server;
    @Override
    public void run() {
        try {
            server.start();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
