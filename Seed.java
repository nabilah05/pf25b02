public enum Seed {   // to save as "Seed.java"
    CROSS, NOUGHT, NO_SEED;
    
    /**
     * Returns the opposite player seed (CROSS ↔ NOUGHT)
     * @return opposite seed
     * @throws IllegalStateException if called on NO_SEED
     */
    public Seed opposite() {
        switch (this) {
            case CROSS: return NOUGHT;
            case NOUGHT: return CROSS;
            default: 
                throw new IllegalStateException("NO_SEED doesn't have an opposite");
        }
    }
}