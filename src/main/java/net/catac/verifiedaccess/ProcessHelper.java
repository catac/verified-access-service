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
public class ProcessHelper {
    private static final Logger logger = LoggerFactory.getLogger(ProcessHelper.class);

    private String readAllLines(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines()
                .collect(Collectors.joining("\n"));
    }

    public String runCAScript(String script, String input) throws InterruptedException, IOException {
        logger.info("Running ca script " + script + " with input:\n=====\n" + input + "=====");
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("sh", "-c", script);
        Process proc = pb.start();
        if (input != null) {
            Writer w = new OutputStreamWriter(proc.getOutputStream()).append(input);
            w.flush();
            w.close();
        }
        int exit = proc.waitFor();
        String output = readAllLines(proc.getInputStream());
        String error = readAllLines(proc.getErrorStream());
        String logLine = "Finished running script " + script +
                " with exit code " + exit +
                " and output:\n====\n" + output +
                "\n====\nand error:\n====\n" + error +
                "\n====";
        if (exit != 0) {
            logger.error(logLine);
        } else{
            logger.info(logLine);
        }
        return output;
    }
}
