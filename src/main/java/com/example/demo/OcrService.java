package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.ocr.Client;
import com.aliyun.ocr.models.*;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;


/**
 * @author renyiyong
 * @date 2020/09/28
 */

@Service
public class OcrService {

    private Client ocrClient;
    private RuntimeOptions runtime;

    @Value("${viapi.accessKeyId}")
    private String accessKeyId;
    @Value("${viapi.accessKeySecret}")
    private String accessKeySecret;

    @PostConstruct
    private void init() throws Exception {
        Config config = new Config();
        config.type = "access_key";
        config.regionId = "cn-shanghai";
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.endpoint = "ocr.cn-shanghai.aliyuncs.com";

        ocrClient = new Client(config);
        runtime = new RuntimeOptions();
    }


    /*
    * 身份证识别
    * @param filePath 待识别图片路径
    * @param side     正面/反面
    * @return Map<String,String>
    * @throws Exception 异常
    */
    public Map<String, String> recognizeIdCard(String filePath, String side) throws Exception {
        RecognizeIdentityCardAdvanceRequest request = new RecognizeIdentityCardAdvanceRequest();
        request.imageURLObject = Files.newInputStream(Paths.get(filePath));
        request.side = side;
        RecognizeIdentityCardResponse response = ocrClient.recognizeIdentityCardAdvance(request, runtime);

        if ("face".equals(side)) {
            return JSON.parseObject(JSON.toJSONString(response.data.frontResult), new TypeReference<Map<String, String>>() {});
        } else {
            return JSON.parseObject(JSON.toJSONString(response.data.backResult), new TypeReference<Map<String, String>>() {});
        }
    }

    /*
     * 火车票识别
     * @param filePath 待识别图片路径
     * @return Map<String,String>
     * @throws Exception 异常
     */
    public Map<String, String> recognizeTrainTicket(String filePath) throws Exception {
        RecognizeTrainTicketAdvanceRequest request = new RecognizeTrainTicketAdvanceRequest();
        request.imageURLObject = Files.newInputStream(Paths.get(filePath));
        RecognizeTrainTicketResponse response = ocrClient.recognizeTrainTicketAdvance(request, runtime);
        return JSON.parseObject(JSON.toJSONString(response.data), new TypeReference<Map<String, String>>() {});
    }

    /*
     * 名片识别
     * @param filePath 待识别图片路径
     * @return Map<String,String>
     * @throws Exception 异常
     */
    public Map<String, Object> recognizeBusinessCard(String filePath) throws Exception {
        RecognizeBusinessCardAdvanceRequest request = new RecognizeBusinessCardAdvanceRequest();
        request.imageURLObject = Files.newInputStream(Paths.get(filePath));
        RecognizeBusinessCardResponse response = ocrClient.recognizeBusinessCardAdvance(request, runtime);
        return JSON.parseObject(JSON.toJSONString(response.data), new TypeReference<Map<String, Object>>() {});
    }




}

