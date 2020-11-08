package net.catac.verifiedaccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CAHelper {
    private static final Logger logger = LoggerFactory.getLogger(CAHelper.class);

    private String readAllLines(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String lines = br.lines().collect(Collectors.joining("\n"));
        return lines;
    }

    public String runCAScript(String script, String input) throws InterruptedException, IOException {
        logger.info("Running ca script " + script + " with input:\n=====\n" + input + "=====");
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("sh", "-c", "ca/" + script);
        Process proc = pb.start();
        if (input != null) {
            Writer w = new OutputStreamWriter(proc.getOutputStream()).append(input);
            w.flush();
            w.close();
        }
        int exit = proc.waitFor();
        if (exit != 0) {
            logger.error("Failed refreshing running CA script " + script);
        }
        String output = readAllLines(proc.getInputStream());
        String error = readAllLines(proc.getErrorStream());
        StringBuilder sb = new StringBuilder();
        sb.append("Finished running script ").append(script)
            .append(" with exit code ").append(exit)
            .append(" and output:\n====\n").append(output)
            .append("\n====\nand error:\n====\n").append(error)
            .append("\n====");
        logger.info(sb.toString());
        return output;
    }
}
