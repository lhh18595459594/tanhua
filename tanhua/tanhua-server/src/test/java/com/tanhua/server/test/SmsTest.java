package com.tanhua.server.test;

import com.tanhua.commons.templates.SmsTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {

    @Autowired
    private SmsTemplate smsTemplate;

    @Test
    public void testSms(){
        smsTemplate.sendValidateCode("18595459594","9999");
    }
}
