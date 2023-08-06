package com.ssgh.sdpgateway.mtls.clienttocontroller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ClientStarter implements Runnable{
    @Resource
    Client client;
    @Override
    public void run() {
        try {
            client.connect();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
