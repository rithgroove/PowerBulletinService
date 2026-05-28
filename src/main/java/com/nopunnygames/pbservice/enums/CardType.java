package com.nopunnygames.pbservice.enums;

/**
 * Gameplay timing category for a Power Bulletin card version.
 */
public enum CardType {
    /** A card played as the active player's normal action. */
    ACTION,
    /** A card played in response to another effect. */
    REACTION,
    /** A card whose effect resolves when it is discarded. */
    ON_DISCARD
}
