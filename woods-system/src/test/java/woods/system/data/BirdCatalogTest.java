package woods.system.data;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BirdCatalogTest {

    private final BirdCatalog catalog = new BirdCatalog();

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void noBounds_returnsWholeFacility() {
        assertEquals(4, catalog.search("F-100", null, null, null, null).size());
    }

    @Test
    void exactPrice_returnsBothBirdsAtThatPrice() {
        BigDecimal price = new BigDecimal("250");
        assertEquals(2, catalog.search("F-100", price, price, null, null).size());
    }

    @Test
    void unknownFacility_isEmptyNotAnError() {
        assertEquals(0, catalog.search("F-999", null, null, null, null).size());
    }

    @Test
    void lastTwelveMonths_leavesOutTheSparrow() {
        // Robin (20 days), Blue tit (3 months), Magpie (8 months); not Sparrow (14 months)
        assertEquals(3, catalog.search("F-100", null, null, TODAY.minusMonths(12), TODAY).size());
    }

    @Test
    void lastSixMonths_findsRobinAndBlueTit() {
        assertEquals(2, catalog.search("F-100", null, null, TODAY.minusMonths(6), TODAY).size());
    }
}
