package com.example.fileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


@SpringBootApplication
@EnableScheduling
public class FileshareApplication {

    public static void main(String[] args) {

        System.setProperty("spring.main.banner-mode", "off");
        SpringApplication.run(FileshareApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        String ip = getLocalIPv4();
        int port = 8080;

        System.out.println("\n=== SECURE FILE TRANSFER ===");
        System.out.println("Server: http://" + ip + ":" + port);
        System.out.println("Status: http://" + ip + ":" + port + "/api/files/status");
        System.out.println("\n📤 Upload: POST /api/files/upload");
        System.out.println("📥 Download: GET /api/files/download/{fileId}");
        System.out.println("\n⚠️  Privacy Mode: Encrypted, RAM-only, Auto-delete");
        System.out.println("============================\n");
    }

    private String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr.getAddress().length == 4 && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();

                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return "localhost";
    }
}















