package de.grobox.transportr.data.dto

enum class KProduct(val c: Char) {
    HIGH_SPEED_TRAIN('I'),
    REGIONAL_TRAIN('R'),
    SUBURBAN_TRAIN('S'),
    SUBWAY('U'),
    TRAM('T'),
    BUS('B'),
    FERRY('F'),
    CABLECAR('C'),
    ON_DEMAND('P'),
    FOOTWAY('W'),
    TRANSFER('T'),
    SECURE_CONNECTION('E'),
    DO_NOT_CHANGE('D'),
    UNKNOWN('?');

    companion object {
        val ALL = KProduct.entries.toSet()

        fun fromCode(c: Char): KProduct = KProduct.entries.find { it.c == c } ?: throw IllegalArgumentException("Unknown product code: $c")
        fun fromCodes(c: CharArray): Set<KProduct> = c.map { fromCode(it) }.toSet()
        fun Set<KProduct>.toCodes(): CharArray = this.map { it.c }.toCharArray()
    }
}