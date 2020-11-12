package net.catac.verifiedaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

@RestController
public class FilesController {
    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);

    @Value("${files.dir}")
    private String filesDir;

    @PostConstruct
    private void setup() throws IOException, GeneralSecurityException {
        logger.info("Serving files from " + filesDir + " ...");
    }

    private MediaType findContentType(Path path, HttpServletRequest request) {
        String fullName = path.toAbsolutePath().toFile().getAbsolutePath();
        String contentType = request.getServletContext().getMimeType(fullName);
        if(contentType == null){
            contentType = "application/octet-stream";
        }
        return MediaType.parseMediaType(contentType);
    }

    @RequestMapping("/files/{fileName}")
    public ResponseEntity<byte[]> file(@PathVariable("fileName") String fileName, HttpServletRequest request) throws IOException {
        String sanitizedName = fileName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        Path path = Paths.get(filesDir + "/" + sanitizedName);
        if (Files.notExists(path)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "File not found"
            );
        }

        return ResponseEntity
                .ok()
                .contentType(findContentType(path, request))
                .body(Files.readAllBytes(path));
    }
}
