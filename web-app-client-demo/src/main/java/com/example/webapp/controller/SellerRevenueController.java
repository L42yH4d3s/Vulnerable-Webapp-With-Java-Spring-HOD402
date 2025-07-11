package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.example.webapp.util.SantitizeXml;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/seller/revenue")
public class SellerRevenueController {

    @GetMapping("/upload")
    public String showUploadForm() {
        return "seller/revenue/upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null){
                redirectAttributes.addFlashAttribute("message", "File "+file.getOriginalFilename()+" không tồn tại");
            }
            if (fileName != null && fileName.toLowerCase().endsWith(".xml")) {
                //Validate DOCTYPE
           /* String originalXml = new String(file.getBytes(), StandardCharsets.UTF_8);
        String sanitizedXml = SantitizeXml.removeDoctype(originalXml);
        InputStream sanitizedInputStream = new ByteArrayInputStream(sanitizedXml.getBytes(StandardCharsets.UTF_8));*/
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setXIncludeAware(true);
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file.getInputStream());
                String month = getTagValue(doc, "month");
                String data = getTagValue(doc, "data");
                redirectAttributes.addFlashAttribute("message", "✅ Upload file " +file.getOriginalFilename()+ " thành công!");
                redirectAttributes.addFlashAttribute("month", month);
                redirectAttributes.addFlashAttribute("data", data);
            } else {
                redirectAttributes.addFlashAttribute("message", "✅ Upload file " +file.getOriginalFilename()+ " thành công!");
                return "redirect:/seller/revenue/upload";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Lỗi xử lý XML: " + e.getMessage());
            return "redirect:/seller/revenue/upload";
        }

        return "redirect:/seller/revenue/upload";
    }
    private String getTagValue(Document doc, String tagName) throws Exception {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0) != null) {
            return nodeList.item(0).getTextContent();
        } else {
            throw new Exception("Không tìm thấy thẻ bắt buộc: '<" + tagName + ">' trong file XML.");
        }
    }
}
