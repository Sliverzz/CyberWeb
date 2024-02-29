package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.services.ProductService;
import com.sean.cyberweb.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> listProducts(@RequestParam("draw") int draw,
                                                            @RequestParam("start") int start,
                                                            @RequestParam("length") int length,
                                                            @RequestParam(value = "search[value]", defaultValue = "") String searchValue) {
        int pageSize = length == -1 ? Integer.MAX_VALUE : length;
        PageRequest pageRequest = PageRequest.of(start / pageSize, pageSize);
        Page<Product> productPage = searchValue.isEmpty() ?
                productService.findAll(pageRequest) :
                productService.findByNameContainingIgnoreCase(searchValue, pageRequest);

        Map<String, Object> data = new HashMap<>();
        data.put("draw", draw);
        data.put("recordsTotal", productPage.getTotalElements());
        data.put("recordsFiltered", productPage.getTotalElements());
        data.put("data", productPage.getContent());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product, @RequestParam(value = "productImage") MultipartFile file,
                                HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            // flashMessage與重導
            WebUtils.addFlashMessage(redirectAttributes, "warning", "Please select a file to upload.");
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
        }
        try {
            productService.saveOrUpdateProduct(product, file);
            WebUtils.addFlashMessage(redirectAttributes, "success", "You successfully uploaded !");

        } catch (IOException e) {
            e.printStackTrace();
            WebUtils.addFlashMessage(redirectAttributes, "error", "Could not upload the file: " + file.getOriginalFilename() + "!");
        }

        return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
    }

    @GetMapping("/productInfo")
    @ResponseBody
    public Product productInfo(@RequestParam("id") Long id) {
        return productService.findProductById(id);
    }

    @GetMapping("/fetchProducts")
    public ResponseEntity<Page<Product>> listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Product> products = productService.findAllActiveProducts(PageRequest.of(page, size));
        return ResponseEntity.ok(products);
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product , @RequestParam(value = "productImage") MultipartFile file,
                                HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            productService.saveOrUpdateProduct(product, file);
            WebUtils.addFlashMessage(redirectAttributes, "success", "Product updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            WebUtils.addFlashMessage(redirectAttributes, "error", "Error updating product: " + e.getMessage());
        }
        return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("id") Long id,HttpServletRequest request,RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            WebUtils.addFlashMessage(redirectAttributes, "success", "The product has been deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            WebUtils.addFlashMessage(redirectAttributes, "error", "An error occurred while deleting the product：" + e.getMessage());
        }
        return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
    }
}
