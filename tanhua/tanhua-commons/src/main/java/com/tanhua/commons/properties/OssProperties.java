package com.tanhua.commons.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 从配置文件中获取阿里云OSS存储配置信息
 */
@Data
@ConfigurationProperties(prefix = "tanhua.oss")
public class OssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String url;//sztanhua.oss-cn-shenzhen.aliyuncs.com
}
