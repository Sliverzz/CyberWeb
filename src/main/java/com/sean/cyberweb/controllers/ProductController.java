package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.services.ProductService;
import com.sean.cyberweb.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 查所有產品
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/listAll")
    public ResponseEntity<List<Product>> listAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    // 新增產品
    @PreAuthorize("hasRole('ADMIN')")
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

    // 商品資訊
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/productInfo")
    @ResponseBody
    public Product productInfo(@RequestParam("id") Long id) {
        return productService.findProductById(id);
    }

    // 商品頁迭代產品
    @GetMapping("/fetchProducts")
    public ResponseEntity<Page<Product>> listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Product> products = productService.findAllActiveProducts(PageRequest.of(page, size));
        return ResponseEntity.ok(products);
    }

    // 更新
    @PreAuthorize("hasRole('ADMIN')")
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

    // 刪除
    @PreAuthorize("hasRole('ADMIN')")
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
