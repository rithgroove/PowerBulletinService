package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.ProductItemDto;
import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.pbservice.entity.Product;
import com.nopunnygames.pbservice.entity.ProductItem;
import com.nopunnygames.pbservice.repository.CardPrintSetRepository;
import com.nopunnygames.pbservice.repository.ProductItemRepository;
import com.nopunnygames.pbservice.repository.ProductRepository;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for product item CRUD and merge behavior.
 */
@Service
public class ProductItemService extends MasterService<ProductItem, UUID, ProductItemDto> {
    private final ProductItemRepository repository;
    private final ProductRepository productRepository;
    private final CardPrintSetRepository cardPrintSetRepository;

    /**
     * Creates the product item service.
     *
     * @param repository product item repository
     * @param productRepository product repository
     * @param cardPrintSetRepository card print set repository
     */
    public ProductItemService(
            ProductItemRepository repository,
            ProductRepository productRepository,
            CardPrintSetRepository cardPrintSetRepository
    ) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.cardPrintSetRepository = cardPrintSetRepository;
    }

    @Override
    protected ProductItemRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<ProductItem> getEntityClass() {
        return ProductItem.class;
    }

    @Override
    protected Class<ProductItemDto> getDtoClass() {
        return ProductItemDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(ProductItemDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(ProductItemDto dto) {
        return validate(dto);
    }

    @Override
    @Transactional
    public ProductItemDto create(ProductItemDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        Product product = product(dto.getProductId());
        CardPrintSet cardPrintSet = cardPrintSet(dto.getCardPrintSetId());

        ProductItem entity = repository.findActiveByProductIdAndCardPrintSetId(product.getId(), cardPrintSet.getId())
                .orElseGet(ProductItem::new);
        if (entity.getId() == null) {
            entity.setProduct(product);
            entity.setCardPrintSet(cardPrintSet);
        }
        entity.setQuantity(1);
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(defaultStatus(dto.getStatus(), "Active"));
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public ProductItemDto update(ProductItemDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        ProductItem entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Product item " + id + " not found"));
        Product product = product(dto.getProductId());
        CardPrintSet cardPrintSet = cardPrintSet(dto.getCardPrintSetId());
        repository.findActiveByProductIdAndCardPrintSetId(product.getId(), cardPrintSet.getId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ValidationErrorException("Update Failed", List.of(new ValidationError("cardPrintSetId", "Product already contains this card print set.")));
                });

        entity.setProduct(product);
        entity.setCardPrintSet(cardPrintSet);
        entity.setQuantity(1);
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(defaultStatus(dto.getStatus(), entity.getStatus()));
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Lists active items for one product.
     *
     * @param productId product UUID
     * @return active item DTOs
     */
    @Transactional(readOnly = true)
    public List<ProductItemDto> listByProduct(UUID productId) {
        product(productId);
        return repository.findActiveByProductId(productId)
                .stream()
                .map(ProductItem::toCompleteDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductItemDto delete(UUID id, AuthUser user) {
        ProductItem entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Product item " + id + " not found"));
        entity.setDeletedAt(LocalDateTime.now());
        if (user != null) {
            entity.setDeletedByID(UUID.fromString(user.userId()));
            entity.setDeletedByName(user.userName());
        }
        return repository.save(entity).toCompleteDto();
    }

    private Product product(UUID productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getDeletedAt() == null)
                .orElseThrow(() -> new ObjectNotFoundException("Product " + productId + " not found"));
    }

    private CardPrintSet cardPrintSet(UUID cardPrintSetId) {
        return cardPrintSetRepository.findById(cardPrintSetId)
                .filter(printSet -> printSet.getDeletedAt() == null)
                .orElseThrow(() -> new ObjectNotFoundException("Card print set " + cardPrintSetId + " not found"));
    }

    private List<ValidationError> validate(ProductItemDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (dto.getProductId() == null) errors.add(new ValidationError("productId", "Product is required."));
        if (dto.getCardPrintSetId() == null) errors.add(new ValidationError("cardPrintSetId", "Card print set is required."));
        return errors;
    }

    private String defaultStatus(String status, String fallback) {
        return isBlank(status) ? fallback : status;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
