package framework.http;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.Part;

public class MultipartFile {
    private final String fieldName;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final Part part;

    public MultipartFile(String fieldName, Part part) {
        this.fieldName = fieldName;
        this.part = part;
        this.originalFilename = part != null ? part.getSubmittedFileName() : null;
        this.contentType = part != null ? part.getContentType() : null;
        this.size = part != null ? part.getSize() : 0L;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public boolean isEmpty() {
        return part == null || size <= 0;
    }

    public InputStream getInputStream() throws IOException {
        if (part == null) return InputStream.nullInputStream();
        return part.getInputStream();
    }

    public void transferTo(java.io.File dest) throws IOException {
        if (part == null) return;
        try (InputStream in = part.getInputStream();
             java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
