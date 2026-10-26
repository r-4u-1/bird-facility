package woods.system.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

/**
 * Woods' OWN description of its XML messages, as JAXB classes.
 *
 * The bird facility has its own, separate copies of these shapes
 * (BirdsListRequest, WoodsBirdsListResponse ...). Neither side imports the
 * other's classes: they only agree on the XML. The tag names below must
 * therefore match the ones the bird facility sends and expects.
 *
 * They are grouped as nested classes in one file because they only make sense
 * together. JAXB rules for nested classes: they must be "static" (not tied to
 * an outer instance) and need a no-argument constructor (Java provides one
 * when you write none).
 */
public final class WoodsXml {

    private WoodsXml() {
        // Only a container for the classes below; never instantiated.
    }

    // ------------------------------------------------------------------ request

    /**
     * What woods receives:
     *   <BirdsListRequest>
     *     <Body><FacilityId>F-100</FacilityId><FromAmount></FromAmount><ToAmount></ToAmount></Body>
     *   </BirdsListRequest>
     */
    @XmlRootElement(name = "BirdsListRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BirdsListRequest {
        @XmlElement(name = "Body")
        public RequestBody body;
    }

    /** The search fields. Woods' rule: an empty string means "no filter". */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RequestBody {
        @XmlElement(name = "FacilityId") public String facilityId;
        @XmlElement(name = "FromAmount") public String fromAmount;
        @XmlElement(name = "ToAmount")   public String toAmount;
    }

    // ----------------------------------------------------------------- response

    /**
     * What woods answers:
     *   <BirdsListResponse>
     *     <Body><Birds><Bird>...</Bird></Birds></Body>
     *   </BirdsListResponse>
     */
    @XmlRootElement(name = "BirdsListResponse")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BirdsListResponse {
        @XmlElement(name = "Body")
        public ResponseBody body;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseBody {
        @XmlElementWrapper(name = "Birds")   // the <Birds> element around the list
        @XmlElement(name = "Bird")           // one <Bird> per item
        public List<BirdElement> birds;
    }

    /** One <Bird>. propOrder fixes the element order in the output. */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"id", "name", "species", "price", "foodType", "foodGramsPerDay", "feathers"})
    public static class BirdElement {
        @XmlElement(name = "Id")              public String id;
        @XmlElement(name = "Name")            public String name;
        @XmlElement(name = "Species")         public String species;
        @XmlElement(name = "Price")           public String price;
        @XmlElement(name = "FoodType")        public String foodType;
        @XmlElement(name = "FoodGramsPerDay") public String foodGramsPerDay;
        @XmlElement(name = "Feathers")        public FeathersElement feathers;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"type", "lengthMm", "primaryColor", "secondaryColor"})
    public static class FeathersElement {
        @XmlElement(name = "Type")           public String type;
        @XmlElement(name = "LengthMm")       public String lengthMm;
        @XmlElement(name = "PrimaryColor")   public String primaryColor;
        @XmlElement(name = "SecondaryColor") public String secondaryColor;
    }

    // -------------------------------------------------------------------- error

    /**
     * What woods answers when the request is wrong (with HTTP 400):
     *   <WoodsError><Code>MISSING_FACILITY</Code><Message>...</Message></WoodsError>
     */
    @XmlRootElement(name = "WoodsError")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class WoodsError {
        @XmlElement(name = "Code")    public String code;
        @XmlElement(name = "Message") public String message;

        /** JAXB needs a no-argument constructor once we write another one. */
        public WoodsError() {
        }

        public WoodsError(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
