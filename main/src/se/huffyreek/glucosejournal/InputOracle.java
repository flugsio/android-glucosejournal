package se.huffyreek.glucosejournal;

class InputOracle {
    // GT greater than
    // GE greater or equal
    // EQ equal
    // NE not equal
    // LT less than
    // LE less than or equal

    // conditions
    private Integer lengthGE;

    public InputOracle() {
    }

    public InputOracle lengthGE(Integer value) {
        this.lengthGE = value;
        return this;
    }

    public boolean check(String s) {
        return (lengthGE != null && s.length() >= lengthGE);
    }
}

