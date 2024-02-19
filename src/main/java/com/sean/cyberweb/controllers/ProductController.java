package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> listProducts(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length) {
        PageRequest pageRequest = PageRequest.of(start / length, length);
        Page<Product> productPage = productService.findAll(pageRequest);

        Map<String, Object> data = new HashMap<>();
        data.put("draw", draw);
        data.put("recordsTotal", productPage.getTotalElements());
        data.put("recordsFiltered", productPage.getTotalElements());
        data.put("data", productPage.getContent());

        return data;
    }

    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product, @RequestParam(value = "productImage") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("flashMessage", "Please select a file to upload.");
            return "redirect:/dashboard/product";
        }
        try {
            productService.saveOrUpdateProduct(product, file);
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "You successfully uploaded !");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Could not upload the file: " + file.getOriginalFilename() + "!");
        }

        return "redirect:/dashboard/product";
    }

    @GetMapping("/productInfo")
    @ResponseBody
    public Product productInfo(@RequestParam("id") Long id) {
        return productService.findProductById(id);
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product , @RequestParam(value = "productImage") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            productService.saveOrUpdateProduct(product, file);
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Product updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Error updating product: " + e.getMessage());
        }
        return "redirect:/dashboard/product";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "商品已被刪除。");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "刪除商品時發生錯誤：" + e.getMessage());
        }
        return "redirect:/dashboard/product";
    }
}
