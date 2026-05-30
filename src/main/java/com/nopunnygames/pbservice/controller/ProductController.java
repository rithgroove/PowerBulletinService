package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.ProductDto;
import com.nopunnygames.pbservice.dto.ProductItemDto;
import com.nopunnygames.pbservice.dto.ProductSummaryDto;
import com.nopunnygames.pbservice.entity.Product;
import com.nopunnygames.pbservice.repository.ProductRepository;
import com.nopunnygames.pbservice.service.ProductItemService;
import com.nopunnygames.pbservice.service.ProductService;
import com.nopunnygames.tanuki.core.controller.MasterController;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.response.ApiErrorResponse;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for products.
 */
@RestController
@RequestMapping("/products")
public class ProductController extends MasterController<Product, UUID, ProductDto> {
    private final ProductService service;
    private final ProductItemService productItemService;
    private final ProductRepository repository;

    /**
     * Creates the product controller.
     *
     * @param service product service
     * @param productItemService product item service
     * @param repository product repository
     */
    public ProductController(ProductService service, ProductItemService productItemService, ProductRepository repository) {
        this.service = service;
        this.productItemService = productItemService;
        this.repository = repository;
    }

    @Override
    protected ProductService getService() {
        return service;
    }

    /**
     * Reads a product by stable code.
     *
     * @param code stable code
     * @return product response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ProductDto>> getByCode(@PathVariable String code) {
        Product product = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Product " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, product.toCompleteDto()));
    }

    /**
     * Lists active items for one product.
     *
     * @param productId product UUID
     * @return product item list response
     */
    @GetMapping("/{productId}/items")
    public ResponseEntity<ApiResponse<List<ProductItemDto>>> items(@PathVariable UUID productId) {
        return ResponseEntity.ok(new ApiResponse<>(200, productItemService.listByProduct(productId)));
    }

    /**
     * Adds or merges a card print set into a product.
     *
     * @param productId product UUID
     * @param dto request DTO
     * @param authentication Spring Security authentication
     * @return product item response
     */
    @PostMapping("/{productId}/items")
    public ResponseEntity<ApiResponse<ProductItemDto>> addItem(
            @PathVariable UUID productId,
            @RequestBody ProductItemDto dto,
            Authentication authentication
    ) {
        AuthUser user = getAuthenticatedUser(authentication);
        ResponseEntity<ApiResponse<ProductItemDto>> permissionCheck = checkPermission(user, permissionCode("UPDATE"));
        if (permissionCheck != null) {
            return permissionCheck;
        }

        dto.setProductId(productId);
        try {
            return ResponseEntity.ok(new ApiResponse<>(200, productItemService.create(dto, user)));
        } catch (ValidationErrorException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse<>(400, dto, exception.getMessage(), exception.errors));
        }
    }

    /**
     * Updates one product item.
     *
     * @param productId product UUID
     * @param itemId product item UUID
     * @param dto request DTO
     * @param authentication Spring Security authentication
     * @return product item response
     */
    @PutMapping("/{productId}/items/{itemId}")
    public ResponseEntity<ApiResponse<ProductItemDto>> updateItem(
            @PathVariable UUID productId,
            @PathVariable UUID itemId,
            @RequestBody ProductItemDto dto,
            Authentication authentication
    ) {
        AuthUser user = getAuthenticatedUser(authentication);
        ResponseEntity<ApiResponse<ProductItemDto>> permissionCheck = checkPermission(user, permissionCode("UPDATE"));
        if (permissionCheck != null) {
            return permissionCheck;
        }

        dto.setProductId(productId);
        try {
            return ResponseEntity.ok(new ApiResponse<>(200, productItemService.update(dto, itemId, user)));
        } catch (ValidationErrorException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse<>(400, dto, exception.getMessage(), exception.errors));
        }
    }

    /**
     * Soft deletes one product item.
     *
     * @param productId product UUID
     * @param itemId product item UUID
     * @param authentication Spring Security authentication
     * @return deleted item response
     */
    @DeleteMapping("/{productId}/items/{itemId}")
    public ResponseEntity<ApiResponse<ProductItemDto>> removeItem(
            @PathVariable UUID productId,
            @PathVariable UUID itemId,
            Authentication authentication
    ) {
        AuthUser user = getAuthenticatedUser(authentication);
        ResponseEntity<ApiResponse<ProductItemDto>> permissionCheck = checkPermission(user, permissionCode("UPDATE"));
        if (permissionCheck != null) {
            return permissionCheck;
        }

        return ResponseEntity.ok(new ApiResponse<>(200, productItemService.delete(itemId, user)));
    }

    /**
     * Returns an analysis-ready product summary.
     *
     * @param productId product UUID
     * @return product summary response
     */
    @GetMapping("/{productId}/summary")
    public ResponseEntity<ApiResponse<ProductSummaryDto>> summary(@PathVariable UUID productId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.summary(productId)));
    }

    @Override
    protected String getFeatureCode() {
        return "PB_PRODUCT";
    }
}
