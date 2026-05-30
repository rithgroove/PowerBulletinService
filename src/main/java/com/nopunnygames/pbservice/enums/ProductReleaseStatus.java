package com.nopunnygames.pbservice.enums;

/**
 * Publishing state for a Power Bulletin product.
 */
public enum ProductReleaseStatus {
    /** Product is still being prepared. */
    DRAFT,
    /** Product can be used for deck construction. */
    ACTIVE,
    /** Product is retained for history but should not be applied to new decks. */
    ARCHIVED
}
