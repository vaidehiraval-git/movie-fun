package org.superbiz.moviefun.blobstore;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        String bucketName = "moviefun";//to be changed
        try {
            // Create a minioClient with the MinIO Server name, Port, Access key and Secret key.
            MinioClient minioClient = new MinioClient("http://127.0.0.1:9000", "AAAAAAAAA", "zuf+SSSSSSSSS");//tobe changed
           // MinioClient minioClient = new MinioClient("http://127.0.0.1:9000", "AAAAAAAAA", "zuf+SSSSSSSSS");

            // Check if the bucket already exists.
            boolean isExist = minioClient.bucketExists(bucketName);
            if(isExist) {
                System.out.println("Bucket already exists.");
            } else {
                // Make a new bucket called asiatrip to hold a zip file of photos.
                minioClient.makeBucket(bucketName);
            }

            // Upload the zip file to the bucket with putObject
            minioClient.putObject(bucketName,blob.name, blob.inputStream,blob.contentType);
            System.out.println("blob.name is successfully uploaded as blob.name to bucket.");
        } catch(MinioException | NoSuchAlgorithmException | InvalidKeyException | XmlPullParserException e) {
            System.out.println("Error occurred: " + e);
        }

    }

    public HttpEntity<byte[]> createImageHttpHeaders(Optional<Blob> blob) throws IOException {
        byte[] imageBytes = new byte[blob.get().inputStream.available()];
        blob.get().inputStream.read(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }



    @Override
    public Optional<Blob> get(String name) throws IOException {
        String bucketName = "moviefun";//to be changed
        Optional<Blob> b = Optional.empty();
        try {
           MinioClient minioClient = new MinioClient("http://127.0.0.1:9000", "AAAAAAAAA", "zuf+SSSSSSSSS");//tobe changed

            // Check if the bucket already exists.
            boolean isExist = minioClient.bucketExists(bucketName);
            if(isExist) {
                System.out.println("Bucket already exists.");

                // Upload the zip file to the bucket with putObject
                InputStream is = minioClient.getObject(bucketName,name);
                //Blob blob = new Blob(name, is, );

            }

        } catch(MinioException | NoSuchAlgorithmException | InvalidKeyException | XmlPullParserException e) {
            System.out.println("Error occurred: " + e);
        }
        return b;
    }

    @Override
    public void deleteAll() {
        // ...
    }

    public void saveUploadToFile(MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }
    public HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    public File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    public Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

}