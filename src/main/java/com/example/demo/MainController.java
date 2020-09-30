package com.example.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;

import com.aliyun.tea.TeaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * @author renyiyong
 * @date 2020/09/28
 */
@Controller
@RequestMapping("/")
public class MainController {


    //身份证
    private static final String  TYPE_IDCARD = "RecognizeIdentityCard";
    //火车票
    private static final String  TYPE_TRAINTICKET = "RecognizeTrainTicket";
    //名片
    private static final String  TYPE_BUSINESSCARD = "RecognizeBusinessCard";

    private String type;
    private String uploadDirectory;
    private OcrService ocrService;
    private List<String> faceImages;
    private List<String> backImages;
    private List<String> ticketImages;
    private List<String> bussImages;
    private List<Map<String, String>> faceResults;
    private List<Map<String, String>> backResults;
    private List<Map<String, String>> ticketResults;
    private List<Map<String, Object>> bussResults;

    public MainController(@Value("${file.upload.path}") String uploadDirectory, OcrService ocrService) {
        this.uploadDirectory = uploadDirectory;
        this.ocrService = ocrService;
        faceImages = new ArrayList<>();
        backImages = new ArrayList<>();
        ticketImages = new ArrayList<>();
        bussImages = new ArrayList<>();
        faceResults = new ArrayList<>();
        backResults = new ArrayList<>();
        ticketResults = new ArrayList<>();
        bussResults = new ArrayList<>();
    }

    private String saveFile(MultipartFile file) throws Exception {
        String suffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
        String filename = UUID.randomUUID().toString() + "." + suffix;
        Path path = Paths.get(uploadDirectory + filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    @RequestMapping()
    public String index(Model model) {
        if (!CollectionUtils.isEmpty(faceImages) && faceImages.size() == backImages.size()) {
            model.addAttribute("faceImage", faceImages.get(faceImages.size() - 1));
            model.addAttribute("faceResult", faceResults.get(faceResults.size() - 1));
            model.addAttribute("backImage", backImages.get(backImages.size() - 1));
            model.addAttribute("backResult", backResults.get(backResults.size() - 1));
        }else if(!CollectionUtils.isEmpty(ticketImages) ){
            model.addAttribute("ticketImages", ticketImages.get(ticketImages.size() - 1));
            model.addAttribute("ticketResults", ticketResults.get(ticketResults.size() - 1));
        }else if(!CollectionUtils.isEmpty(bussImages) ){
            model.addAttribute("bussImages", bussImages.get(bussImages.size() - 1));
            model.addAttribute("bussResults", bussResults.get(bussResults.size() - 1));
        }
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("type") String type , @RequestParam("face") MultipartFile face, @RequestParam(required = false,value="back") MultipartFile back, RedirectAttributes attributes) {
        if (face.isEmpty() && back.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        String errorMessage = null;
        try {
            Path dir = Paths.get(uploadDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            if (!face.isEmpty()) {
                String filename = saveFile(face);
                if(TYPE_IDCARD.equalsIgnoreCase(type)){
                    Map<String, String> res = ocrService.recognizeIdCard(uploadDirectory + filename, "face");
                    faceImages.add("/images/" + filename);
                    faceResults.add(res);

                    bussImages.clear();
                    bussResults.clear();
                    ticketImages.clear();
                    ticketResults.clear();
                }else if(TYPE_TRAINTICKET.equalsIgnoreCase(type)){
                    Map<String, String> res = ocrService.recognizeTrainTicket(uploadDirectory + filename);
                    ticketImages.add("/images/" + filename);
                    ticketResults.add(res);

                    bussImages.clear();
                    bussResults.clear();
                    faceImages.clear();
                    backImages.clear();
                    faceResults.clear();
                    backResults.clear();
                }else if(TYPE_BUSINESSCARD.equalsIgnoreCase(type)){
                    Map<String, Object> res = ocrService.recognizeBusinessCard(uploadDirectory + filename);
                    bussImages.add("/images/" + filename);
                    bussResults.add(res);

                    ticketImages.clear();
                    ticketResults.clear();
                    faceImages.clear();
                    backImages.clear();
                    faceResults.clear();
                    backResults.clear();
                }

            }
            if (back != null && !back.isEmpty()) {
                String filename = saveFile(back);
                Map<String, String> res = ocrService.recognizeIdCard(uploadDirectory + filename, "back");
                backImages.add("/images/" + filename);
                backResults.add(res);
            }
        } catch (TeaException e) {
            e.printStackTrace();
            errorMessage = JSON.toJSONString(e.getData());
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            attributes.addFlashAttribute("message", errorMessage);
        }
        return "redirect:/";
    }
}

