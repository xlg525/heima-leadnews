package com.heima.common.fastdfs;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FastDFSClientUtil {

    @Autowired
    private FastFileStorageClient storageClient;

    /**
     * 上传
     * @param file
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile((InputStream) file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return storePath.getFullPath();
    }

    /**
     * 删除
     * @param filePath
     */
    public void delFile(String filePath) {
        storageClient.deleteFile(filePath);

    }

    /**
     * 下载
     * @param groupName
     * @param path
     * @return
     */
    public byte[] download(String groupName, String path) throws IOException {
        InputStream ins = storageClient.downloadFile(groupName, path, new DownloadCallback<InputStream>() {
            @Override
            public InputStream recv(InputStream ins) throws IOException {
                // 将此ins返回给上面的ins
                return ins;
            }
        });

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = ins.read(buff, 0, 100)) > 0) {
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }
}