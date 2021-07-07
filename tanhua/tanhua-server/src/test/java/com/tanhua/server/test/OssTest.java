package com.tanhua.server.test;

import cn.hutool.core.date.DateUtil;
import com.tanhua.commons.templates.OssTemplate;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OssTest {

    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void testOss() throws FileNotFoundException {
        FileInputStream is = new FileInputStream("C:\\woman\\6.jpg");
        ossTemplate.upload("6.jpg",is);
    }

}
