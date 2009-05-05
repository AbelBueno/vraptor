package br.com.caelum.vraptor.interceptor.multipart;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.http.MutableRequest;

/**
 * Processes all elements in a multipart request.
 *
 * @author Guilherme Silveira
 * @author Paulo Silveira
 */
public class MultipartItemsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MultipartItemsProcessor.class);
    private final List<FileItem> items;
    private final HttpServletRequest request;
    private final MutableRequest parameters;

    public MultipartItemsProcessor(List<FileItem> items, HttpServletRequest request, MutableRequest parameters) {
        this.items = items;
        this.request = request;
        this.parameters = parameters;
    }

    public void process() {
        for (FileItem item : items) {
            if (item.isFormField()) {
                parameters.setParameter(item.getFieldName(), new String[] { item.getString() });
                continue;
            }
            if (!item.getName().trim().equals("")) {
                try {
                    File file = File.createTempFile("raptor.", ".upload");
                    file.deleteOnExit();
                    item.write(file);
                    UploadedFile fileInformation = new DefaultUploadedFile(file, item.getName(),
                            item.getContentType());
                    parameters.setParameter(item.getFieldName(), new String[] { file.getAbsolutePath() });
                    request.setAttribute(file.getAbsolutePath(), fileInformation);
                    logger.info("Uploaded file: " + item.getFieldName() + " with " + fileInformation);
                } catch (Exception e) {
                    logger.error("Nasty uploaded file " + item.getName(), e);
                }
            } else {
                logger.info("A file field was empy: " + item.getFieldName());
            }
        }
    }
}