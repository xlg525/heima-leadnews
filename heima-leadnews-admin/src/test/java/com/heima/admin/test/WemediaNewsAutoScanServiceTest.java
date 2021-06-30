package com.heima.admin.test;

import com.heima.admin.AdminApplication;
import com.heima.admin.service.WemediaNewsAutoScanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class WemediaNewsAutoScanServiceTest {

    @Autowired
    private WemediaNewsAutoScanService wemediaNewsAutoScanService;

    @Test
    public void testScanNews(){
        wemediaNewsAutoScanService.autoScanByMediaNewsId(6219);
    }
}