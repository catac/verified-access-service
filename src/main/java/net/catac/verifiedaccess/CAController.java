package net.catac.verifiedaccess;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.Exception;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class CAController {
    private static final Logger logger = LoggerFactory.getLogger(CAController.class);

    @Scheduled(fixedDelay = 24L * 60 * 60 * 1000)
    public void scheduleFixedDelayTask() throws Exception {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("sh", "-c", "ca/crl.sh");
        int exit = pb.start().waitFor();
        if (exit != 0){
            logger.error("Failed refreshing crl");
        }
    }

    // The CA in PEM format
    @RequestMapping("/ca.crt")
    public byte[] ca_pem() throws IOException {
        return Files.readAllBytes(Paths.get("ca/ca.crt"));
    }

    // The CRL in DER format
    @RequestMapping("/ca.crl")
    public byte[] ca_crl() throws IOException {
        return Files.readAllBytes(Paths.get("ca/ca.crl"));
    }
}
