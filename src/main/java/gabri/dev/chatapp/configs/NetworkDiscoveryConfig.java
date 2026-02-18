package gabri.dev.chatapp.configs;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@Component
@Slf4j
public class NetworkDiscoveryConfig implements ApplicationRunner {

    @Value("${server.port}")
    private int port;

    @Override
    public void run(ApplicationArguments args) {
        String ip = getLocalIp();
        String url = "http://" + ip + ":" + port;

        printBanner(url);
        printQrToConsole(url);
        registerMdns(ip);
    }

    private void printBanner(String url) {
        log.info("╔═══════════════════════════════════════════╗");
        log.info("║           🥕 CarrotChat iniciado           ║");
        log.info("╠═══════════════════════════════════════════╣");
        log.info("║  URL local:  {}  ║", url);
        log.info("║  mDNS:       http://carrotchat.local:{}  ║", port);
        log.info("╚═══════════════════════════════════════════╝");
        log.info("  Escaneá el QR para conectarte desde tu celu:");
    }

    private void registerMdns(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            JmDNS jmdns = JmDNS.create(address);
            ServiceInfo serviceInfo = ServiceInfo.create(
                    "_http._tcp.local.",
                    "CarrotChat",
                    port,
                    "CarrotChat - Chat LAN"
            );
            jmdns.registerService(serviceInfo);
            log.info("✅ mDNS registrado — accedé via http://carrotchat.local:{}", port);
        } catch (Exception e) {
            // mDNS es opcional, no debe romper la app si falla
            log.warn("⚠️  mDNS no disponible en este entorno: {}", e.getMessage());
            log.info("   Usá la URL directa o el QR para conectarte.");
        }
    }

    private String getLocalIp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            log.warn("No se pudo detectar IP local: {}", e.getMessage());
            return "localhost";
        }
    }

    private void printQrToConsole(String url) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    url, BarcodeFormat.QR_CODE, 35, 35
            );
            System.out.println(); // línea en blanco antes del QR
            for (int y = 0; y < matrix.getHeight(); y++) {
                StringBuilder row = new StringBuilder("  ");
                for (int x = 0; x < matrix.getWidth(); x++) {
                    row.append(matrix.get(x, y) ? "██" : "  ");
                }
                System.out.println(row);
            }
            System.out.println();
        } catch (Exception e) {
            log.warn("No se pudo generar el QR: {}", e.getMessage());
        }
    }
}