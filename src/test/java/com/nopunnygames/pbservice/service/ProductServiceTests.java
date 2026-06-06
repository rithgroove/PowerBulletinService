package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.dto.CardPrintSetDto;
import com.nopunnygames.pbservice.dto.CardVersionDto;
import com.nopunnygames.pbservice.dto.DeckEntryDto;
import com.nopunnygames.pbservice.dto.DeckIdentityDto;
import com.nopunnygames.pbservice.dto.DeckVersionDto;
import com.nopunnygames.pbservice.dto.DeckVersionProductDto;
import com.nopunnygames.pbservice.dto.ProductDeckApplicationDto;
import com.nopunnygames.pbservice.dto.ProductDto;
import com.nopunnygames.pbservice.dto.ProductItemDto;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.enums.CardType;
import com.nopunnygames.pbservice.enums.Faction;
import com.nopunnygames.pbservice.enums.ProductReleaseStatus;
import com.nopunnygames.pbservice.enums.ProductType;
import com.nopunnygames.pbservice.repository.DeckEntryRepository;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.response.PagedResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service tests for product catalog behavior.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pb_service_product_tests;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "seed.power-bulletin.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class ProductServiceTests {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductItemService productItemService;

    @Autowired
    private EffectDefinitionService effectDefinitionService;

    @Autowired
    private CardIdentityService cardIdentityService;

    @Autowired
    private CardVersionService cardVersionService;

    @Autowired
    private CardPrintSetService cardPrintSetService;

    @Autowired
    private DeckIdentityService deckIdentityService;

    @Autowired
    private DeckVersionService deckVersionService;

    @Autowired
    private DeckEntryService deckEntryService;

    @Autowired
    private DeckEntryRepository deckEntryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createsListsSearchesUpdatesAndDeletesProduct() {
        ProductDto product = productService.create(product("CORE_ALPHA", "Power Bulletin Core"), null);

        Pageable pageable = productService.buildPageRequest(0, 10, "code", "asc");
        PagedResponse<ProductDto> searchResult = productService.getAll(Map.of(), "core", true, pageable, null);
        assertThat(searchResult.getItems()).extracting(ProductDto::getCode).contains(product.getCode());

        product.setName("Power Bulletin Core Updated");
        ProductDto updated = productService.update(product, product.getId(), null);
        assertThat(updated.getName()).isEqualTo("Power Bulletin Core Updated");

        productService.delete(product.getId(), null);
        assertThatThrownBy(() -> productService.getOne(product.getId(), null))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void addsProductItemAndKeepsDuplicatePrintSetQuantityAtOne() {
        ProductDto product = productService.create(product("CORE_DUPLICATE", "Core Duplicate"), null);
        CardPrintSetDto printSet = createPrintSet("DUPLICATE_CARD");

        ProductItemDto first = productItem(product.getId(), printSet.getId(), 1);
        ProductItemDto created = productItemService.create(first, null);
        assertThat(created.getQuantity()).isEqualTo(1);

        ProductItemDto second = productItem(product.getId(), printSet.getId(), 2);
        ProductItemDto merged = productItemService.create(second, null);
        assertThat(merged.getId()).isEqualTo(created.getId());
        assertThat(merged.getQuantity()).isEqualTo(1);
        assertThat(productItemService.listByProduct(product.getId())).hasSize(1);
    }

    @Test
    void forcesInvalidProductItemQuantityToOne() {
        ProductDto product = productService.create(product("CORE_INVALID_ITEM", "Core Invalid Item"), null);
        CardPrintSetDto printSet = createPrintSet("INVALID_ITEM_CARD");

        ProductItemDto item = productItem(product.getId(), printSet.getId(), 0);
        ProductItemDto created = productItemService.create(item, null);
        assertThat(created.getQuantity()).isEqualTo(1);
    }

    @Test
    void softDeletesProductItem() {
        ProductDto product = productService.create(product("CORE_DELETE_ITEM", "Core Delete Item"), null);
        CardPrintSetDto printSet = createPrintSet("DELETE_ITEM_CARD");
        ProductItemDto item = productItemService.create(productItem(product.getId(), printSet.getId(), 1), null);

        ProductItemDto deleted = productItemService.delete(item.getId(), null);

        assertThat(deleted.getId()).isEqualTo(item.getId());
        assertThat(productItemService.listByProduct(product.getId())).isEmpty();
    }

    @Test
    void linksProductToDeckVersionWithoutChangingDeckEntries() {
        ProductDto product = productService.create(product("CORE_LINK_ONLY", "Core Link Only"), null);
        DeckVersionDto deckVersion = createDeckVersion("LINK_ONLY_DECK");

        DeckVersionProductDto linked = productService.linkProductToDeckVersion(deckVersion.getId(), product.getId(), null);
        DeckVersionProductDto linkedAgain = productService.linkProductToDeckVersion(deckVersion.getId(), product.getId(), null);

        assertThat(linked.getProductId()).isEqualTo(product.getId());
        assertThat(linked.getProductCode()).isEqualTo(product.getCode());
        assertThat(linkedAgain.getQuantityMultiplier()).isEqualTo(1);
        assertThat(productService.listLinkedProducts(deckVersion.getId())).hasSize(1);
        assertThat(deckEntryRepository.findByDeckVersionIdOrderByIdAsc(deckVersion.getId())).isEmpty();

        productService.unlinkProductFromDeckVersion(deckVersion.getId(), product.getId(), null);

        assertThat(productService.listLinkedProducts(deckVersion.getId())).isEmpty();
    }

    @Test
    void softDeletesDeckEntryWithResolvedPrintSet() {
        DeckVersionDto deckVersion = createDeckVersion("DELETE_ENTRY_DECK");
        CardPrintSetDto printSet = createPrintSet("DELETE_ENTRY_CARD");
        DeckEntryDto entry = new DeckEntryDto();
        entry.setDeckVersionId(deckVersion.getId());
        entry.setCardPrintSetId(printSet.getId());
        entry = deckEntryService.create(entry, null);

        DeckEntryDto deleted = deckEntryService.delete(entry.getId(), null);

        assertThat(deleted.getId()).isEqualTo(entry.getId());
        assertThat(deleted.getPrintSet()).isNotNull();
        assertThat(deleted.getPrintSet().getCode()).isEqualTo(printSet.getCode());
        entityManager.flush();
        entityManager.clear();
        Object deletedAt = entityManager.createNativeQuery("SELECT deleted_at FROM deck_entries WHERE id = :id")
                .setParameter("id", entry.getId())
                .getSingleResult();
        assertThat(deletedAt).isNotNull();
    }

    @Test
    void recreatingSoftDeletedDeckEntryRestoresExistingRow() {
        DeckVersionDto deckVersion = createDeckVersion("RESTORE_ENTRY_DECK");
        CardPrintSetDto printSet = createPrintSet("RESTORE_ENTRY_CARD");
        DeckEntryDto entry = new DeckEntryDto();
        entry.setDeckVersionId(deckVersion.getId());
        entry.setCardPrintSetId(printSet.getId());
        entry = deckEntryService.create(entry, null);

        deckEntryService.delete(entry.getId(), null);
        DeckEntryDto restored = new DeckEntryDto();
        restored.setDeckVersionId(deckVersion.getId());
        restored.setCardPrintSetId(printSet.getId());
        restored = deckEntryService.create(restored, null);

        assertThat(restored.getId()).isEqualTo(entry.getId());
        assertThat(restored.getPrintSet()).isNotNull();
        entityManager.flush();
        entityManager.clear();
        Object deletedAt = entityManager.createNativeQuery("SELECT deleted_at FROM deck_entries WHERE id = :id")
                .setParameter("id", entry.getId())
                .getSingleResult();
        assertThat(deletedAt).isNull();
    }

    @Test
    void rejectsDuplicateDeckVersionCodeOnCreate() {
        DeckVersionDto existing = createDeckVersion("DUPLICATE_DECK_VERSION");
        DeckVersionDto duplicate = new DeckVersionDto();
        duplicate.setDeckIdentityId(existing.getDeckIdentityId());
        duplicate.setCode(existing.getCode());
        duplicate.setVersionName("v2");
        duplicate.setStatus("Active");

        assertThatThrownBy(() -> deckVersionService.create(duplicate, null))
                .isInstanceOf(ValidationErrorException.class)
                .hasMessageContaining("Creation Failed");
    }

    @Test
    void allowsDeckVersionUpdateWithoutChangingCode() {
        DeckVersionDto version = createDeckVersion("UNCHANGED_DECK_VERSION");
        version.setVersionName("v2");

        DeckVersionDto updated = deckVersionService.update(version, version.getId(), null);

        assertThat(updated.getCode()).isEqualTo(version.getCode());
        assertThat(updated.getVersionName()).isEqualTo("v2");
    }

    @Test
    void rejectsDuplicateDeckVersionCodeOnUpdate() {
        DeckVersionDto first = createDeckVersion("UPDATE_DUPLICATE_FIRST");
        DeckVersionDto second = createDeckVersion("UPDATE_DUPLICATE_SECOND");
        second.setCode(first.getCode());

        assertThatThrownBy(() -> deckVersionService.update(second, second.getId(), null))
                .isInstanceOf(ValidationErrorException.class)
                .hasMessageContaining("Update Failed");
    }

    @Test
    void applyingProductCreatesDeckEntries() {
        ProductDto product = productService.create(product("CORE_APPLY_CREATE", "Core Apply Create"), null);
        CardPrintSetDto first = createPrintSet("APPLY_CREATE_ONE");
        CardPrintSetDto second = createPrintSet("APPLY_CREATE_TWO");
        productItemService.create(productItem(product.getId(), first.getId(), 2), null);
        productItemService.create(productItem(product.getId(), second.getId(), 1), null);
        DeckVersionDto deckVersion = createDeckVersion("APPLY_CREATE_DECK");

        ProductDeckApplicationDto result = productService.addProductToDeckVersion(deckVersion.getId(), product.getId(), null);

        assertThat(result.getItemsAdded()).isEqualTo(2);
        assertThat(result.getEntriesCreated()).isEqualTo(2);
        assertThat(result.getEntriesUpdated()).isZero();
        assertThat(result.getTotalQuantityAdded()).isEqualTo(2);
        assertThat(deckEntryRepository.findByDeckVersionIdOrderByIdAsc(deckVersion.getId())).hasSize(2);
    }

    @Test
    void applyingProductMergesExistingDeckEntryQuantity() {
        ProductDto product = productService.create(product("CORE_APPLY_MERGE", "Core Apply Merge"), null);
        CardPrintSetDto printSet = createPrintSet("APPLY_MERGE_CARD");
        productItemService.create(productItem(product.getId(), printSet.getId(), 2), null);
        DeckVersionDto deckVersion = createDeckVersion("APPLY_MERGE_DECK");

        productService.addProductToDeckVersion(deckVersion.getId(), product.getId(), null);
        ProductDeckApplicationDto secondApply = productService.addProductToDeckVersion(deckVersion.getId(), product.getId(), null);

        List<DeckEntry> entries = deckEntryRepository.findByDeckVersionIdOrderByIdAsc(deckVersion.getId());
        assertThat(secondApply.getEntriesCreated()).isZero();
        assertThat(secondApply.getEntriesUpdated()).isZero();
        assertThat(secondApply.getTotalQuantityAdded()).isZero();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getQuantity()).isEqualTo(1);
    }

    @Test
    void rejectsApplyingEmptyProduct() {
        ProductDto product = productService.create(product("CORE_EMPTY", "Core Empty"), null);
        DeckVersionDto deckVersion = createDeckVersion("EMPTY_DECK");

        assertThatThrownBy(() -> productService.addProductToDeckVersion(deckVersion.getId(), product.getId(), null))
                .isInstanceOf(ValidationErrorException.class)
                .hasMessageContaining("Product Cannot Be Added");
    }

    @Test
    void rejectsApplyingInactiveProduct() {
        ProductDto product = product("CORE_INACTIVE", "Core Inactive");
        product.setStatus("Inactive");
        product = productService.create(product, null);
        CardPrintSetDto printSet = createPrintSet("INACTIVE_CARD");
        productItemService.create(productItem(product.getId(), printSet.getId(), 1), null);
        DeckVersionDto deckVersion = createDeckVersion("INACTIVE_DECK");

        ProductDto inactiveProduct = product;
        assertThatThrownBy(() -> productService.addProductToDeckVersion(deckVersion.getId(), inactiveProduct.getId(), null))
                .isInstanceOf(ValidationErrorException.class);
    }

    private ProductDto product(String code, String name) {
        ProductDto dto = new ProductDto();
        dto.setCode(code + "_" + UUID.randomUUID().toString().substring(0, 8));
        dto.setName(name);
        dto.setDescription(name + " description");
        dto.setProductType(ProductType.CORE);
        dto.setReleaseStatus(ProductReleaseStatus.ACTIVE);
        dto.setDisplayOrder(1);
        dto.setStatus("Active");
        return dto;
    }

    private ProductItemDto productItem(UUID productId, UUID cardPrintSetId, int quantity) {
        ProductItemDto dto = new ProductItemDto();
        dto.setProductId(productId);
        dto.setCardPrintSetId(cardPrintSetId);
        dto.setQuantity(quantity);
        dto.setSortOrder(1);
        dto.setStatus("Active");
        return dto;
    }

    private CardPrintSetDto createPrintSet(String codePrefix) {
        var effect = new com.nopunnygames.pbservice.dto.EffectDefinitionDto();
        effect.setCode(codePrefix + "_EFFECT_" + UUID.randomUUID().toString().substring(0, 8));
        effect.setName(codePrefix + " Effect");
        effect.setPythonClass("power_bulletin.effects.test." + codePrefix.toLowerCase());
        effect.setDescription("Test effect");
        effect.setStatus("Active");
        effect = effectDefinitionService.create(effect, null);

        CardIdentityDto card = new CardIdentityDto();
        card.setCode(codePrefix + "_CARD_" + UUID.randomUUID().toString().substring(0, 8));
        card.setName(codePrefix + " Card");
        card.setCharacterName(codePrefix + " Character");
        card.setFaction(Faction.HERO);
        card.setStatus("Active");
        card = cardIdentityService.create(card, null);

        CardVersionDto version = new CardVersionDto();
        version.setCardIdentityId(card.getId());
        version.setEffectDefinitionId(effect.getId());
        version.setCode(codePrefix + "_VERSION_" + UUID.randomUUID().toString().substring(0, 8));
        version.setVersionName("v1");
        version.setCardType(CardType.ACTION);
        version.setEffectText("Test effect text");
        version.setStatus("Active");
        version = cardVersionService.create(version, null);

        CardPrintSetDto printSet = new CardPrintSetDto();
        printSet.setCardVersionId(version.getId());
        printSet.setCode(codePrefix + "_PRINT_" + UUID.randomUUID().toString().substring(0, 8));
        printSet.setPowers(List.of(1, 2, 3, 4));
        printSet.setStatus("Active");
        return cardPrintSetService.create(printSet, null);
    }

    private DeckVersionDto createDeckVersion(String codePrefix) {
        DeckIdentityDto identity = new DeckIdentityDto();
        identity.setCode(codePrefix + "_IDENTITY_" + UUID.randomUUID().toString().substring(0, 8));
        identity.setName(codePrefix + " Deck");
        identity.setStatus("Active");
        identity = deckIdentityService.create(identity, null);

        DeckVersionDto version = new DeckVersionDto();
        version.setDeckIdentityId(identity.getId());
        version.setCode(codePrefix + "_VERSION_" + UUID.randomUUID().toString().substring(0, 8));
        version.setVersionName("v1");
        version.setStatus("Active");
        return deckVersionService.create(version, null);
    }
}
