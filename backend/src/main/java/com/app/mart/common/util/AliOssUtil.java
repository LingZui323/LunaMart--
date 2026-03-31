package com.app.mart.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 阿里云 OSS 云存储工具类
 * 功能：实现图片/文件的上传、删除，用于商品图片、头像等资源存储
 * 配置信息从 application.yml 中自动读取
 *
 * @author LunaMart
 */
@Component
public class AliOssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.folder}")
    private String folder;

    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    /**
     * 上传文件到阿里云 OSS
     * 自动生成 UUID 文件名，避免重复，返回可访问的完整 URL
     *
     * @param file 上传的文件
     * @return 文件访问 URL
     * @throws IOException 文件流异常
     */
    public String upload(MultipartFile file) throws IOException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String originalFilename = file.getOriginalFilename();
        // 修复空指针：文件名不存在时给默认值
        if (originalFilename == null) {
            originalFilename = UUID.randomUUID().toString();
        }

        String suffix = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String fileName = folder + UUID.randomUUID() + suffix;
        ossClient.putObject(bucketName, fileName, file.getInputStream());
        ossClient.shutdown();
        return urlPrefix + fileName;
    }

    /**
     * 根据文件 URL 删除 OSS 上的文件
     * 会自动校验 URL 是否属于当前 Bucket，避免误删外部文件
     *
     * @param imageUrl 文件访问 URL
     */
    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(urlPrefix)) {
            return;
        }

        String objectName = imageUrl.substring(urlPrefix.length());
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.deleteObject(bucketName, objectName);
        ossClient.shutdown();
    }
}