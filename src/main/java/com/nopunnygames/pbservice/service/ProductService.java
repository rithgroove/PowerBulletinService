package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.ProductDeckApplicationDto;
import com.nopunnygames.pbservice.dto.ProductDto;
import com.nopunnygames.pbservice.dto.ProductItemDto;
import com.nopunnygames.pbservice.dto.ProductSummaryDto;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.entity.DeckVersionProduct;
import com.nopunnygames.pbservice.entity.Product;
import com.nopunnygames.pbservice.entity.ProductItem;
import com.nopunnygames.pbservice.enums.ProductReleaseStatus;
import com.nopunnygames.pbservice.enums.ProductType;
import com.nopunnygames.pbservice.repository.DeckEntryRepository;
import com.nopunnygames.pbservice.repository.DeckVersionProductRepository;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.pbservice.repository.ProductItemRepository;
import com.nopunnygames.pbservice.repository.ProductRepository;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.response.PageMeta;
import com.nopunnygames.tanuki.core.response.PagedResponse;
import com.nopunnygames.tanuki.core.util.SpecificationBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for product CRUD, summaries, and deck application behavior.
 */
@Service
public class ProductService extends MasterService<Product, UUID, ProductDto> {
    private final ProductRepository repository;
    private final ProductItemRepository productItemRepository;
    private final DeckVersionRepository deckVersionRepository;
    private final DeckEntryRepository deckEntryRepository;
    private final DeckVersionProductRepository deckVersionProductRepository;

    /**
     * Creates the product service.
     *
     * @param repository product repository
     * @param productItemRepository product item repository
     * @param deckVersionRepository deck version repository
     * @param deckEntryRepository deck entry repository
     * @param deckVersionProductRepository deck version product repository
     */
    public ProductService(
            ProductRepository repository,
            ProductItemRepository productItemRepository,
            DeckVersionRepository deckVersionRepository,
            DeckEntryRepository deckEntryRepository,
            DeckVersionProductRepository deckVersionProductRepository
    ) {
        this.repository = repository;
        this.productItemRepository = productItemRepository;
        this.deckVersionRepository = deckVersionRepository;
        this.deckEntryRepository = deckEntryRepository;
        this.deckVersionProductRepository = deckVersionProductRepository;
    }

    @Override
    protected ProductRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    @Override
    protected Class<ProductDto> getDtoClass() {
        return ProductDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(ProductDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(ProductDto dto) {
        return validate(dto);
    }

    @Override
    public PagedResponse<ProductDto> getAll(
            Map<String, List<String>> filters,
            String search,
            boolean isPaginate,
            Pageable pageable,
            AuthUser user
    ) {
        SpecificationBuilder<Product> specBuilder = new SpecificationBuilder<>();
        Specification<Product> filterSpec = specBuilder.buildFilter(filters, getAllowedFilterFields());
        Specification<Product> searchSpec = productSearch(search);
        Specification<Product> notDeletedSpec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        Specification<Product> finalSpec = (root, query, cb) -> {
            Predicate p1 = filterSpec == null ? cb.conjunction() : filterSpec.toPredicate(root, query, cb);
            Predicate p2 = searchSpec == null ? cb.conjunction() : searchSpec.toPredicate(root, query, cb);
            Predicate p3 = notDeletedSpec.toPredicate(root, query, cb);
            return cb.and(p1, p2, p3);
        };

        if (!isPaginate) {
            List<ProductDto> list = repository.findAll(finalSpec, pageable.getSort())
                    .stream()
                    .map(Product::toDto)
                    .toList();
            return new PagedResponse<>(list, new PageMeta(1, list.size(), list.size(), 1));
        }

        Page<ProductDto> page = repository.findAll(finalSpec, pageable).map(Product::toDto);
        return new PagedResponse<>(
                page.getContent(),
                new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getOne(UUID id, AuthUser user) {
        Product product = findProduct(id);
        return product.toCompleteDto();
    }

    @Override
    @Transactional
    public ProductDto create(ProductDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        Product entity = new Product();
        fillEntity(entity, dto);
        entity.setStatus(defaultStatus(dto.getStatus(), "Active"));
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public ProductDto update(ProductDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        Product entity = findProduct(id);
        fillEntity(entity, dto);
        entity.setStatus(defaultStatus(dto.getStatus(), entity.getStatus()));
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Applies an active product to a deck version and merges deck entries.
     *
     * @param deckVersionId deck version UUID
     * @param productId product UUID
     * @param user authenticated user
     * @return application summary
     */
    @Transactional
    public ProductDeckApplicationDto addProductToDeckVersion(UUID deckVersionId, UUID productId, AuthUser user) {
        DeckVersion deckVersion = deckVersionRepository.findById(deckVersionId)
                .filter(version -> version.getDeletedAt() == null)
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + deckVersionId + " not found"));
        Product product = findProduct(productId);
        validateProductCanBeApplied(product);

        List<ProductItem> items = productItemRepository.findActiveByProductId(productId);
        if (items.isEmpty()) {
            throw new ValidationErrorException("Product Cannot Be Added", List.of(new ValidationError("productId", "Product has no active items.")));
        }

        int created = 0;
        int updated = 0;
        int totalQuantity = 0;
        for (ProductItem item : items) {
            DeckEntry entry = deckEntryRepository.findByDeckVersionIdAndCardPrintSetId(deckVersionId, item.getCardPrintSet().getId())
                    .orElseGet(DeckEntry::new);
            if (entry.getId() == null) {
                entry.setDeckVersion(deckVersion);
                entry.setCardPrintSet(item.getCardPrintSet());
                entry.setQuantity(1);
                created++;
                totalQuantity++;
            } else {
                if (entry.getQuantity() != 1) {
                    entry.setQuantity(1);
                    updated++;
                }
            }
            deckEntryRepository.save(entry);
        }

        DeckVersionProduct provenance = deckVersionProductRepository.findActiveByDeckVersionIdAndProductId(deckVersionId, productId)
                .orElseGet(DeckVersionProduct::new);
        if (provenance.getId() == null) {
            provenance.setDeckVersion(deckVersion);
            provenance.setProduct(product);
            provenance.setQuantityMultiplier(1);
            provenance.setStatus("Active");
        } else {
            provenance.setQuantityMultiplier(provenance.getQuantityMultiplier() + 1);
        }
        provenance.setAppliedAt(LocalDateTime.now());
        deckVersionProductRepository.save(provenance);

        ProductDeckApplicationDto result = new ProductDeckApplicationDto();
        result.setDeckVersionId(deckVersionId);
        result.setProductId(productId);
        result.setProductCode(product.getCode());
        result.setProductName(product.getName());
        result.setItemsAdded(items.size());
        result.setEntriesCreated(created);
        result.setEntriesUpdated(updated);
        result.setTotalQuantityAdded(totalQuantity);
        return result;
    }

    /**
     * Builds a catalog summary for one product.
     *
     * @param productId product UUID
     * @return summary DTO
     */
    @Transactional(readOnly = true)
    public ProductSummaryDto summary(UUID productId) {
        Product product = findProduct(productId);
        List<ProductItemDto> cards = productItemRepository.findActiveByProductId(productId)
                .stream()
                .map(ProductItem::toDto)
                .toList();

        ProductSummaryDto summary = new ProductSummaryDto();
        summary.setProductId(product.getId());
        summary.setCode(product.getCode());
        summary.setName(product.getName());
        summary.setCardPrintSetCount(cards.size());
        summary.setTotalCardQuantity(cards.stream().mapToInt(ProductItemDto::getQuantity).sum());
        summary.setIncludedCards(cards);

        int powerCount = 0;
        int powerTotal = 0;
        Integer minPower = null;
        Integer maxPower = null;
        Map<String, Integer> factions = new LinkedHashMap<>();
        Map<String, Integer> cardTypes = new LinkedHashMap<>();
        for (ProductItemDto item : cards) {
            if (item.getFaction() != null) {
                factions.merge(item.getFaction().name(), item.getQuantity(), Integer::sum);
            }
            if (item.getCardType() != null) {
                cardTypes.merge(item.getCardType().name(), item.getQuantity(), Integer::sum);
            }
            for (Integer power : item.getPowers()) {
                if (power == null) {
                    continue;
                }
                powerCount++;
                powerTotal += power;
                minPower = minPower == null ? power : Math.min(minPower, power);
                maxPower = maxPower == null ? power : Math.max(maxPower, power);
            }
        }
        summary.setFactionBreakdown(factions);
        summary.setCardTypeBreakdown(cardTypes);
        summary.setMinPower(minPower);
        summary.setMaxPower(maxPower);
        if (powerCount > 0) {
            summary.setAveragePower(BigDecimal.valueOf(powerTotal).divide(BigDecimal.valueOf(powerCount), 2, RoundingMode.HALF_UP));
        }
        return summary;
    }

    /**
     * Lists active products with complete item data for exports.
     *
     * @return product DTOs
     */
    @Transactional(readOnly = true)
    public List<ProductDto> exportProducts() {
        return repository.findAll()
                .stream()
                .filter(product -> product.getDeletedAt() == null)
                .filter(product -> "Active".equalsIgnoreCase(product.getStatus()))
                .map(Product::toCompleteDto)
                .toList();
    }

    private Product findProduct(UUID productId) {
        return repository.findById(productId)
                .filter(product -> product.getDeletedAt() == null)
                .orElseThrow(() -> new ObjectNotFoundException("Product " + productId + " not found"));
    }

    private void validateProductCanBeApplied(Product product) {
        List<ValidationError> errors = new ArrayList<>();
        if (!"Active".equalsIgnoreCase(product.getStatus())) {
            errors.add(new ValidationError("status", "Product must be active."));
        }
        if (product.getReleaseStatus() != ProductReleaseStatus.ACTIVE) {
            errors.add(new ValidationError("releaseStatus", "Product release status must be ACTIVE."));
        }
        if (!errors.isEmpty()) {
            throw new ValidationErrorException("Product Cannot Be Added", errors);
        }
    }

    private void fillEntity(Product entity, ProductDto dto) {
        entity.setCode(dto.getCode().trim());
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setProductType(dto.getProductType() == null ? ProductType.OTHER : dto.getProductType());
        entity.setReleaseStatus(dto.getReleaseStatus() == null ? ProductReleaseStatus.DRAFT : dto.getReleaseStatus());
        entity.setDisplayOrder(dto.getDisplayOrder());
    }

    private List<ValidationError> validate(ProductDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getName())) errors.add(new ValidationError("name", "Name is required."));
        if (dto.getProductType() == null) errors.add(new ValidationError("productType", "Product type is required."));
        if (dto.getReleaseStatus() == null) errors.add(new ValidationError("releaseStatus", "Release status is required."));
        return errors;
    }

    private String defaultStatus(String status, String fallback) {
        return isBlank(status) ? fallback : status;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Specification<Product> productSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("productType").as(String.class)), like),
                    cb.like(cb.lower(root.get("releaseStatus").as(String.class)), like)
            );
        };
    }
}
