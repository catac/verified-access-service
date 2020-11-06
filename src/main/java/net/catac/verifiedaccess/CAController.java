package net.catac.verifiedaccess;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@RestController
public class CAController {

    // TODO cron crl.sh

    private String fetchCAFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        File myObj = new File(fileName);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            sb.append(myReader.nextLine());
            sb.append('\n');
        }
        return sb.toString();
    }

    @RequestMapping("/ca.pem")
    public String ca() throws IOException {
        return fetchCAFile("ca/ca.crt");
    }

    @RequestMapping("/crl.pem")
    public String crl() throws IOException {
        return fetchCAFile("ca/ca.crl");
    }
}
