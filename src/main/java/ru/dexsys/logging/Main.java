package ru.dexsys.logging;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SLF4JLogger");

        var resultFile = new File(args.length > 0 ? args[1] : "out.docx");

        byte[] pictureData = null;

        try (var httpClient = HttpClients.createDefault()) {
            var response = httpClient.execute(new HttpGet("https://cataas.com/cat/says/I%20love%20Java"));
            pictureData = EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            log.error("Couldn't download image", e);
            System.exit(1);
        }

        log.info("Picture downloaded, size={}", pictureData.length);

        try (var document = new XWPFDocument();
             var output = new FileOutputStream(resultFile)) {
            var paragraph = document.createParagraph();
            var run = paragraph.createRun();

            run.addPicture(new ByteArrayInputStream(pictureData), XWPFDocument.PICTURE_TYPE_JPEG, "image.jpg", Units.toEMU(400), Units.toEMU(400));

            document.write(output);
        } catch (InvalidFormatException e) {
            log.error("Couldn't add picture", e);
            System.exit(1);
        } catch (IOException e) {
            log.error("Couldn't save document", e);
            System.exit(1);
        }

        log.info("Result file {} saved, opening...", resultFile);

        try {
            Desktop.getDesktop().open(resultFile);
        } catch (IOException e) {
            log.error("Couldn't open {} in default viewer", resultFile, e);
        }
    }
}