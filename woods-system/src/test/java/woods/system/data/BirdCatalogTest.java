package woods.system.data;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BirdCatalogTest {

    private final BirdCatalog catalog = new BirdCatalog();

    @Test
    void noBounds_returnsWholeFacility() {
        assertEquals(4, catalog.search("F-100", null, null).size());
    }

    @Test
    void exactPrice_returnsBothBirdsAtThatPrice() {
        BigDecimal price = new BigDecimal("250");
        assertEquals(2, catalog.search("F-100", price, price).size());
    }

    @Test
    void unknownFacility_isEmptyNotAnError() {
        assertEquals(0, catalog.search("F-999", null, null).size());
    }
}
