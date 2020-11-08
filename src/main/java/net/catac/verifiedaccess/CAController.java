package net.catac.verifiedaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.Exception;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class CAController {

    @Autowired
    private CAHelper caHelper;

    @Scheduled(fixedDelay = 24L * 60 * 60 * 1000)
    public void scheduleFixedDelayTask() throws Exception {
        caHelper.runCAScript("crl.sh", "");
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
