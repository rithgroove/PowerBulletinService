package com.nopunnygames.pbservice.enums;

/**
 * Catalog grouping for a Power Bulletin product.
 */
public enum ProductType {
    /** Base product containing the main game print set. */
    CORE,
    /** Product that extends an existing card pool. */
    EXPANSION,
    /** Limited promotional card product. */
    PROMO,
    /** Test or prototype product not intended as a normal release. */
    TEST_SET,
    /** Product type not covered by the named categories. */
    OTHER
}
