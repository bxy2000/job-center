package com.boxy.job.admin.util;

import com.boxy.job.admin.core.util.I18nUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class I18nUtilTest {

    @Test
    public void test(){
        System.out.println(I18nUtil.getString("admin_name"));
        System.out.println(I18nUtil.getMultString("admin_name", "admin_name_full"));
        System.out.println(I18nUtil.getMultString());
    }

}
