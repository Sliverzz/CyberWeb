package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
