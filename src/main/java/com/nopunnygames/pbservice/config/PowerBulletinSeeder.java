package com.nopunnygames.pbservice.config;

import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.pbservice.entity.CardVersion;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.entity.DeckIdentity;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.entity.EffectDefinition;
import com.nopunnygames.pbservice.entity.Product;
import com.nopunnygames.pbservice.entity.ProductItem;
import com.nopunnygames.pbservice.enums.CardType;
import com.nopunnygames.pbservice.enums.Faction;
import com.nopunnygames.pbservice.enums.ProductReleaseStatus;
import com.nopunnygames.pbservice.enums.ProductType;
import com.nopunnygames.pbservice.repository.CardIdentityRepository;
import com.nopunnygames.pbservice.repository.CardPrintSetRepository;
import com.nopunnygames.pbservice.repository.CardVersionRepository;
import com.nopunnygames.pbservice.repository.DeckEntryRepository;
import com.nopunnygames.pbservice.repository.DeckIdentityRepository;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.pbservice.repository.EffectDefinitionRepository;
import com.nopunnygames.pbservice.repository.ProductItemRepository;
import com.nopunnygames.pbservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the canonical Power Bulletin starter data.
 */
@Component
@ConditionalOnProperty(name = "seed.power-bulletin.enabled", havingValue = "true", matchIfMissing = true)
public class PowerBulletinSeeder implements CommandLineRunner {
    private final EffectDefinitionRepository effectRepository;
    private final CardIdentityRepository cardIdentityRepository;
    private final CardVersionRepository cardVersionRepository;
    private final CardPrintSetRepository cardPrintSetRepository;
    private final DeckIdentityRepository deckIdentityRepository;
    private final DeckVersionRepository deckVersionRepository;
    private final DeckEntryRepository deckEntryRepository;
    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;

    /**
     * Creates the seeder.
     *
     * @param effectRepository effect repository
     * @param cardIdentityRepository card identity repository
     * @param cardVersionRepository card version repository
     * @param cardPrintSetRepository card print set repository
     * @param deckIdentityRepository deck identity repository
     * @param deckVersionRepository deck version repository
     * @param deckEntryRepository deck entry repository
     * @param productRepository product repository
     * @param productItemRepository product item repository
     */
    public PowerBulletinSeeder(
            EffectDefinitionRepository effectRepository,
            CardIdentityRepository cardIdentityRepository,
            CardVersionRepository cardVersionRepository,
            CardPrintSetRepository cardPrintSetRepository,
            DeckIdentityRepository deckIdentityRepository,
            DeckVersionRepository deckVersionRepository,
            DeckEntryRepository deckEntryRepository,
            ProductRepository productRepository,
            ProductItemRepository productItemRepository
    ) {
        this.effectRepository = effectRepository;
        this.cardIdentityRepository = cardIdentityRepository;
        this.cardVersionRepository = cardVersionRepository;
        this.cardPrintSetRepository = cardPrintSetRepository;
        this.deckIdentityRepository = deckIdentityRepository;
        this.deckVersionRepository = deckVersionRepository;
        this.deckEntryRepository = deckEntryRepository;
        this.productRepository = productRepository;
        this.productItemRepository = productItemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedEffects();
        seedCards();
        seedProducts();
        seedDecks();
    }

    private void seedEffects() {
        upsertEffect("591ac218-aaea-5110-bdad-85f58546e10c", "HERO_ATTACKER_V1", "power_bulletin.effects.implementations.power_bulletin_v1.hero_attacker_v1_effect.HeroAttackerV1Effect", "Hero Attacker v1", "Target player discards a random card.");
        upsertEffect("ae24ad60-dcdc-5ad0-a020-3370ef6bb4e0", "HERO_HITMAN_V1", "power_bulletin.effects.implementations.power_bulletin_v1.hero_hitman_v1_effect.HeroHitmanV1Effect", "Hero Hitman v1", "Target player discards up to two chosen villain cards. If two are discarded, active player skips draw.");
        upsertEffect("6d34eadc-ae18-5787-a58e-a4d940998404", "HERO_HEALER_V1", "power_bulletin.effects.implementations.power_bulletin_v1.hero_healer_v1_effect.HeroHealerV1Effect", "Hero Healer v1", "If active player has fewer than five cards after playing this card, their next draw phase draws one additional card.");
        upsertEffect("3a23c227-494c-5ac7-b99e-acb0d2e4137f", "HERO_SEER_V1", "power_bulletin.effects.implementations.power_bulletin_v1.hero_seer_v1_effect.HeroSeerV1Effect", "Hero Seer v1", "Active player may put one card from hand on the bottom of the deck, then draws one additional card next draw phase.");
        upsertEffect("a8747426-59ee-52c3-be38-549a2d5da5b4", "HERO_GUARD_V1", "power_bulletin.effects.implementations.power_bulletin_v1.hero_guard_v1_effect.HeroGuardV1Effect", "Hero Guard v1", "Negates a targeted pending effect against this player and draws one card.");
        upsertEffect("e1ca3e7f-3a55-52f2-8837-ce937a3e714b", "VILLAIN_ATTACKER_V1", "power_bulletin.effects.implementations.power_bulletin_v1.villain_attacker_v1_effect.VillainAttackerV1Effect", "Villain Attacker v1", "Target opponent discards one controller-chosen card.");
        upsertEffect("12ee8754-98a3-53dd-8c14-88baa92bc4fd", "VILLAIN_PROVOCATEUR_V1", "power_bulletin.effects.implementations.power_bulletin_v1.villain_provocateur_v1_effect.VillainProvocateurV1Effect", "Villain Provocateur v1", "Active player and target player each reveal a chosen card. Lower power is discarded; ties discard both.");
        upsertEffect("ff28f408-3813-5ac1-8a0b-5bc4312b7ae5", "VILLAIN_GAMBLER_V1", "power_bulletin.effects.implementations.power_bulletin_v1.villain_gambler_v1_effect.VillainGamblerV1Effect", "Villain Gambler v1", "Active player declares a card name. Target opponent discards all cards with that display name.");
        upsertEffect("db1183d2-4cf0-5c28-97ff-dbc6e7050186", "VILLAIN_VENGEANCE_V1", "power_bulletin.effects.implementations.power_bulletin_v1.villain_vengeance_v1_effect.VillainVengeanceV1Effect", "Villain Vengeance v1", "Every player discards one controller-chosen card if able.");
        upsertEffect("9e6c8198-751d-5870-a933-f2c65eb19274", "VILLAIN_TACTICIAN_V1", "power_bulletin.effects.implementations.power_bulletin_v1.villain_tactician_v1_effect.VillainTacticianV1Effect", "Villain Tactician v1", "Negates a pending effect.");
        upsertEffect("34e9bcb3-812d-5bbf-8f1d-b96b1fb0e956", "CIVILIAN_REPORTER_V1", "power_bulletin.effects.implementations.head_office_expansion_v1.civilian_reporter_v1_effect.CivilianReporterV1Effect", "Civilian Reporter v1", "When discarded, skips the current turn draw phase.");
        upsertEffect("949c2bbc-da04-57ad-aaa3-5f4c644c05b9", "CIVILIAN_SPY_V1", "power_bulletin.effects.implementations.head_office_expansion_v1.civilian_spy_v1_effect.CivilianSpyV1Effect", "Civilian Spy v1", "When discarded, the discarded player draws one card.");
        upsertEffect("8a94b1a6-a999-5bc6-a8ee-1903e1bb0068", "VILLAIN_NECROMANCER_V1", "power_bulletin.effects.implementations.necromancer_underdog_v1.villain_necromancer_v1_effect.VillainNecromancerV1Effect", "Villain Necromancer v1", "Copies the effect of a legal action card from the discard pile.");
        upsertEffect("94ebff96-2729-5d5d-86f5-3c7e5736ed57", "HERO_UNDERDOG_V1", "power_bulletin.effects.implementations.necromancer_underdog_v1.hero_underdog_v1_effect.HeroUnderdogV1Effect", "Hero Underdog v1", "Target opponent discards one card with power greater than Underdog's power.");
    }

    private void seedCards() {
        upsertCard("e31cf05e-86d0-44b7-84dd-0651d22fdef8", "HERO_ATTACKER", "ATTACKER", "Miho", Faction.HERO, "d36364a7-b1e6-4937-9edd-908cb8634968", CardType.ACTION, "Target player discard a random card. If a hero card was discarded this way, that player draws a card.", "b457d49d-e2bb-444c-9a5a-f78986bb5f10", List.of(3, 3, 4, 5));
        upsertCard("a902948f-b6da-44b7-82a6-29212b2c2edb", "HERO_HITMAN", "HITMAN", "Cooper", Faction.HERO, "997d89ed-5cc8-42b3-85f6-d439c215bf11", CardType.ACTION, "Look at target player's hand, choose up to two villain cards, discard them. If two cards were discarded this way, skip your draw step.", "8b1d6a66-3fd2-4ef6-88fd-df2195bf625b", List.of(4, 4, 5, 6));
        upsertCard("671995da-376b-44e3-9a3f-90d52845c761", "HERO_HEALER", "HEALER", "Nightingale", Faction.HERO, "2108b673-1953-4784-96a6-d8dfc8d24589", CardType.ACTION, "If you have less than five cards in your hand after playing this card, draw two instead of one on your next draw phase.", "041949b9-c536-48e6-9439-935e4b93aded", List.of(2, 3, 3, 4));
        upsertCard("d45c2bca-7b00-4872-9805-9c3cc8bc863c", "HERO_SEER", "SEER", "Aurora", Faction.HERO, "9f42bd5e-58bd-4bfa-a5d3-ae50fff70165", CardType.ACTION, "Put one card from your hand to the bottom of the deck if you're able to. Draw two cards on your next draw phase.", "4c11360f-3081-4d6c-82d6-70349e0dab24", List.of(1, 2, 3, 3));
        upsertCard("e37f11a9-8e24-4620-8a7f-158419375e5a", "HERO_GUARD", "GUARD", "Milo", Faction.HERO, "6e51c2b8-b650-4e80-821d-1234c6dcd917", CardType.REACTION, "Play this when an opponent plays a card that targets you. Negate the effect and draw 1 card.", "47ee3463-9db8-4437-8fd7-53845d935c2f", List.of(1, 2, 2, 3));
        upsertCard("10135f2d-fe30-415f-bc67-c5f99c6ea3d9", "VILLAIN_ATTACKER", "ATTACKER", "Killa", Faction.VILLAIN, "cf0ffdd6-9ea5-4257-983c-72a6b5b41d06", CardType.ACTION, "Target opponent discard a card.", "c9fb5b9e-029f-419e-95c2-75ccc08fabb0", List.of(4, 5, 5, 6));
        upsertCard("1d1d63f9-f489-4a9c-9a5c-38771cb3fdbb", "VILLAIN_PROVOCATEUR", "PROVOCATEUR", "Miller", Faction.VILLAIN, "b1d22fe0-f5bd-470a-86c3-0e47fb11d3ec", CardType.ACTION, "You and target opponent reveal a card from each respective hand. Discard the card with lower power. In case of a tie discard both.", "a2d5422b-ea9d-43c5-b7cd-5b09818cea1a", List.of(2, 3, 3, 5));
        upsertCard("9a71e160-e1f6-4be4-ae23-7f1c1e65daf4", "VILLAIN_GAMBLER", "GAMBLER", "Victoria", Faction.VILLAIN, "d6870708-4a16-4980-b9c4-6f12c1cbd129", CardType.ACTION, "Choose an opponent and name a card. Look at the target opponent hand. That opponent must discard all cards with the mentioned name.", "b579f7fb-e8e2-4399-ad8c-49bc24a294ce", List.of(3, 4, 4, 5));
        upsertCard("13aa4a8c-587b-4fbe-a7d0-ffe263a4b69d", "VILLAIN_VENGEANCE", "VENGEANCE", "Astaroth", Faction.VILLAIN, "2d028ca9-5ba0-43b6-bcc4-a779e239f318", CardType.ACTION, "Each player must discard a card if able, including you.", "ef9073df-6a3c-48ab-8aee-896516ad3440", List.of(2, 3, 4, 4));
        upsertCard("e0951536-f520-4f5a-bfa5-0b27bb5f63eb", "VILLAIN_TACTICIAN", "TACTICIAN", "Caffe", Faction.VILLAIN, "1de288bf-5dc1-4683-ae7c-e6e93e03ac85", CardType.REACTION, "Negate the effect of a card.", "f82d61ab-baf8-40f0-bd9a-f3ffecc74584", List.of(1, 1, 2, 3));
        upsertCard("08745104-ed64-434f-9be2-44458b205e76", "CIVILIAN_REPORTER", "REPORTER", "Jenna Liss", Faction.CIVILIAN, "86dd5f34-6789-4eff-854c-2ec795d82ef8", CardType.ON_DISCARD, "On discard: Skip this turn draw phase.", "3dcb9ef5-e358-4c9d-b070-9ef81426996d", List.of(3, 3, 4, 5));
        upsertCard("abad2f60-5c85-4d0f-a082-6f2001d7e7fd", "CIVILIAN_SPY", "SPY", "Joe Schmoe / Cyph", Faction.CIVILIAN, "fee9e106-51ba-4ab4-bc39-838dee12016d", CardType.ON_DISCARD, "On discard: Draw a card.", "c7b75c6b-62c9-4dcb-8e3a-07a757e5462f", List.of(2, 2, 3, 4));
        upsertCard("77f4f4c3-b6c6-5cf3-b359-e9d7ad785087", "VILLAIN_NECROMANCER", "NECROMANCER", "Mortis", Faction.VILLAIN, "bf031abd-1627-5ab6-bc7b-4e0119350adc", CardType.ACTION, "Copy the effect of a legal action card from the discard pile. Do not remove that card from the discard pile.", "b1df5dcb-5592-5a85-acf0-fdc338657487", List.of(2, 3, 3, 4));
        upsertCard("c9c55308-ee28-5401-a1b8-fc095668a7bd", "HERO_UNDERDOG", "UNDERDOG", "Rookie", Faction.HERO, "da0a196d-185a-5898-8808-3f2ee27325e5", CardType.ACTION, "Target opponent discards one card with power greater than this card's power.", "06ae35f5-8612-5bf2-919d-cf1a6b1bbdd2", List.of(1, 1, 2, 2));
    }

    private void seedDecks() {
        DeckIdentity standard = upsertDeckIdentity("3d7d32b8-fa8a-401d-a14f-ff188eb46393", "STANDARD_POWER_BULLETIN", "Standard Power Bulletin Deck");
        DeckVersion standardVersion = upsertDeckVersion("dfc3b63b-82fb-49a9-9b0c-1b0ee958d11b", "STANDARD_POWER_BULLETIN_V0_0", standard, "v0.0", "Initial standard deck seed.");
        for (String printSetCode : List.of("HERO_ATTACKER_V1_STANDARD", "HERO_HITMAN_V1_STANDARD", "HERO_HEALER_V1_STANDARD", "HERO_SEER_V1_STANDARD", "HERO_GUARD_V1_STANDARD", "VILLAIN_ATTACKER_V1_STANDARD", "VILLAIN_PROVOCATEUR_V1_STANDARD", "VILLAIN_GAMBLER_V1_STANDARD", "VILLAIN_VENGEANCE_V1_STANDARD", "VILLAIN_TACTICIAN_V1_STANDARD")) {
            upsertDeckEntry(standardVersion, printSetCode);
        }

        DeckIdentity expanded = upsertDeckIdentity("e0b961cb-d6a1-411d-b79f-b65215dbfa0b", "PB_AND_HO_EXP", "Power Bulletin with Head Office Expansion");
        DeckVersion expandedVersion = upsertDeckVersion("10606258-0fef-4d30-98f8-4597af524c22", "PB_AND_HO_EXP_V1", expanded, "Head Office Test Bed v1", "Power Bulletin base set plus Head Office expansion.");
        for (String printSetCode : List.of("CIVILIAN_REPORTER_V1_STANDARD", "CIVILIAN_SPY_V1_STANDARD", "HERO_GUARD_V1_STANDARD", "HERO_ATTACKER_V1_STANDARD", "HERO_HITMAN_V1_STANDARD", "HERO_SEER_V1_STANDARD", "HERO_HEALER_V1_STANDARD", "VILLAIN_GAMBLER_V1_STANDARD", "VILLAIN_ATTACKER_V1_STANDARD", "VILLAIN_PROVOCATEUR_V1_STANDARD", "VILLAIN_TACTICIAN_V1_STANDARD", "VILLAIN_VENGEANCE_V1_STANDARD")) {
            upsertDeckEntry(expandedVersion, printSetCode);
        }
    }

    private void seedProducts() {
        Product core = upsertProduct("POWER_BULLETIN_CORE", "Power Bulletin Core", "Core Power Bulletin product.", ProductType.CORE, 10);
        int sortOrder = 1;
        for (String printSetCode : List.of("HERO_ATTACKER_V1_STANDARD", "HERO_HITMAN_V1_STANDARD", "HERO_HEALER_V1_STANDARD", "HERO_SEER_V1_STANDARD", "HERO_GUARD_V1_STANDARD", "VILLAIN_ATTACKER_V1_STANDARD", "VILLAIN_PROVOCATEUR_V1_STANDARD", "VILLAIN_GAMBLER_V1_STANDARD", "VILLAIN_VENGEANCE_V1_STANDARD", "VILLAIN_TACTICIAN_V1_STANDARD")) {
            upsertProductItem(core, printSetCode, sortOrder++);
        }

        Product headOffice = upsertProduct("HEAD_OFFICE_EXPANSION", "Head Office Expansion", "Head Office expansion product.", ProductType.EXPANSION, 20);
        sortOrder = 1;
        for (String printSetCode : List.of("CIVILIAN_REPORTER_V1_STANDARD", "CIVILIAN_SPY_V1_STANDARD")) {
            upsertProductItem(headOffice, printSetCode, sortOrder++);
        }
    }

    private EffectDefinition upsertEffect(String id, String code, String pythonClass, String name, String description) {
        EffectDefinition effect = effectRepository.findByCode(code).orElseGet(EffectDefinition::new);
        effect.setCode(code);
        effect.setPythonClass(pythonClass);
        effect.setName(name);
        effect.setDescription(description);
        effect.setStatus("Active");
        return effectRepository.save(effect);
    }

    private Product upsertProduct(String code, String name, String description, ProductType productType, int displayOrder) {
        Product product = productRepository.findByCode(code).orElseGet(Product::new);
        product.setCode(code);
        product.setName(name);
        product.setDescription(description);
        product.setProductType(productType);
        product.setReleaseStatus(ProductReleaseStatus.ACTIVE);
        product.setDisplayOrder(displayOrder);
        product.setStatus("Active");
        return productRepository.save(product);
    }

    private void upsertProductItem(Product product, String printSetCode, int sortOrder) {
        CardPrintSet printSet = cardPrintSetRepository.findByCode(printSetCode).orElseThrow();
        ProductItem item = productItemRepository.findActiveByProductIdAndCardPrintSetId(product.getId(), printSet.getId()).orElseGet(ProductItem::new);
        item.setProduct(product);
        item.setCardPrintSet(printSet);
        item.setQuantity(1);
        item.setSortOrder(sortOrder);
        item.setStatus("Active");
        productItemRepository.save(item);
    }

    private void upsertCard(String identityId, String identityCode, String name, String characterName, Faction faction, String versionId, CardType cardType, String effectText, String printSetId, List<Integer> powers) {
        CardIdentity identity = cardIdentityRepository.findByCode(identityCode).orElseGet(CardIdentity::new);
        identity.setCode(identityCode);
        identity.setName(name);
        identity.setCharacterName(characterName);
        identity.setFaction(faction);
        identity.setStatus("Active");
        identity = cardIdentityRepository.save(identity);

        EffectDefinition effect = effectRepository.findByCode(identityCode + "_V1").orElseThrow();
        CardVersion version = cardVersionRepository.findByCode(identityCode + "_V1").orElseGet(CardVersion::new);
        version.setCode(identityCode + "_V1");
        version.setCardIdentity(identity);
        version.setEffectDefinition(effect);
        version.setVersionName("v1");
        version.setCardType(cardType);
        version.setEffectText(effectText);
        version.setStatus("Active");
        version = cardVersionRepository.save(version);

        CardPrintSet printSet = cardPrintSetRepository.findByCode(identityCode + "_V1_STANDARD").orElseGet(CardPrintSet::new);
        printSet.setCode(identityCode + "_V1_STANDARD");
        printSet.setCardVersion(version);
        printSet.setStatus("Active");
        if (!printSet.getPowers().stream().map(power -> power.getPower()).toList().equals(powers)) {
            printSet.replacePowers(powers);
        }
        cardPrintSetRepository.save(printSet);
    }

    private DeckIdentity upsertDeckIdentity(String id, String code, String name) {
        DeckIdentity deck = deckIdentityRepository.findByCode(code).orElseGet(DeckIdentity::new);
        deck.setCode(code);
        deck.setName(name);
        deck.setStatus("Active");
        return deckIdentityRepository.save(deck);
    }

    private DeckVersion upsertDeckVersion(String id, String code, DeckIdentity identity, String versionName, String notes) {
        DeckVersion version = deckVersionRepository.findByCode(code).orElseGet(DeckVersion::new);
        version.setCode(code);
        version.setDeckIdentity(identity);
        version.setVersionName(versionName);
        version.setNotes(notes);
        version.setStatus("Active");
        return deckVersionRepository.save(version);
    }

    private void upsertDeckEntry(DeckVersion deckVersion, String printSetCode) {
        CardPrintSet printSet = cardPrintSetRepository.findByCode(printSetCode).orElseThrow();
        DeckEntry entry = deckEntryRepository.findByDeckVersionIdAndCardPrintSetId(deckVersion.getId(), printSet.getId()).orElseGet(DeckEntry::new);
        entry.setDeckVersion(deckVersion);
        entry.setCardPrintSet(printSet);
        entry.setQuantity(1);
        deckEntryRepository.save(entry);
    }
}
