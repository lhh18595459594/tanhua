package com.tanhua.FastDFS;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.ServerApplication;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
public class FastDFSTest {

    @Autowired
    private FastFileStorageClient client;   //客户端

    @Autowired
    private FdfsWebServer fdfsWebServer;    //nginx，读取

    @Test
    public void testUploadFile() throws IOException {
        File file = new File("C:\\1.jpg");
        StorePath storePath = client.uploadFile(FileUtils.openInputStream(file), file.length(), "jpg", null);
        System.out.println(storePath.getFullPath());
        System.out.println(storePath.getPath());

        //获取文件请求地址
        String url = fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
        System.out.println(url);
    }
}