package com.heima.admin.test;

import com.heima.admin.AdminApplication;
import com.heima.common.aliyun.GreeTextScan;
import com.heima.common.aliyun.GreenImageScan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {

    @Autowired
    private GreeTextScan greeTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

/*    @Autowired
    private FastDFSClient fastDFSClient;*/

    @Test
    public void testText() throws Exception{
        Map map = greeTextScan.greeTextScan("我是一个文本,冰毒买卖是违法的");
        System.out.println(map);
    }

   /* @Test
    public void testImage() throws Exception {
        byte[] image1 = fastDFSClient.download("group1", "M00/00/00/wKjIgl892suAAHHxAACr_szTy3c449.jpg");
        List<byte[]> images = new ArrayList<>();
        images.add(image1);
        Map map = greenImageScan.imageScan(images);
        System.out.println(map);
    }*/
}
