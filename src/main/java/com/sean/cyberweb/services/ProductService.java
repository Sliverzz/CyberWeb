package com.sean.cyberweb.services;

import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;

@Service
public class ProductService {

    // PC圖片上傳位置
    @Value("${imgPathPC}")
    private String UPLOAD_DIR;

    // MAC圖片上傳位置
//  @Value("${imgPathMAC}")
//  private String UPLOAD_DIR;

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Page<Product> findAllActiveProducts(Pageable pageable) {
        return productRepository.findByStatusTrue(pageable);
    }

    public Page<Product> findByNameContainingIgnoreCase(String name, PageRequest pageRequest) {
        return productRepository.findByNameContainingIgnoreCase(name, pageRequest);
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    // 儲存或更新
    public void saveOrUpdateProduct(Product product, MultipartFile file) throws IOException {
        Product targetProduct = product;

        // 檢查產品是否已存在（更新操作）
        if (product.getId() != null) {
            Product existingProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalStateException("產品ID " + product.getId() + " 不存在。"));

            // 更新產品信息
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setStatus(product.isStatus());

            targetProduct = existingProduct;
        }

        // 檢查是否有新的圖片文件上傳
        if (file != null && !file.isEmpty()) {
            String webPath = uploadAndSaveImage(file);
            targetProduct.setProductImagePath(webPath);
        }

        // 保存產品到資料庫
        productRepository.save(targetProduct);
    }

    /**
     * 上傳圖片並返回圖片的web路徑。
     * @param file 圖片文件
     * @return 圖片的web路徑
     * @throws IOException 如果上傳過程出錯
     */
    private String uploadAndSaveImage(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR).resolve(fileName);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return "/img/" + fileName; // 返回圖片的web路徑
    }
}
