package com.ssgh.sdpgateway.spa.clienttocontroller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SPAClientStarter implements Runnable{
    @Resource
    SPAClient spaClient;
    @Override
    public void run() {
        try {
            spaClient.connect();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
