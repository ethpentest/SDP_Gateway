package com.ssgh.sdpgateway;

import com.ssgh.sdpgateway.mtls.clienttocontroller.ClientStarter;

import com.ssgh.sdpgateway.mtls.servertoproxy.ServerStarter;
import com.ssgh.sdpgateway.spa.clienttocontroller.SPAClientStarter;
import com.ssgh.sdpgateway.spa.servertoproxy.SPAServerStarter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;


@SpringBootApplication
@MapperScan("com.ssgh.sdpgateway.mtls.mapper")
public class GatewayDemoApplication implements CommandLineRunner {
    @Resource
    ClientStarter mtlsClientStarter;
    @Resource
    ServerStarter mtlsServerStarter;
    @Resource
    SPAServerStarter spaServerStarter;
    @Resource
    SPAClientStarter spaClientStarter;

    @Override
    public void run(String[] args) {

       // spa Server启动，接受客户端代理的SPA报文
       Thread spaServerThread = new Thread(spaServerStarter);
       spaServerThread.start();

       // mtls Server启动，接受客户端代理的MTLS双向连接
      Thread mtlsServerThread = new Thread(mtlsServerStarter);
      mtlsServerThread.start();

        // spa client启动，向控制器发送SPA报文
        Thread spaClientThread = new Thread(spaClientStarter);
        spaClientThread.start();

        // mtls client启动，与控制器建立MTLS通道，接受控制器下发的访问控制策略
       Thread mtlsClientThread = new Thread(mtlsClientStarter);
       mtlsClientThread.start();
    }
    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }
}
