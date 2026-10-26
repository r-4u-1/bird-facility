package birds.woods.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The OUTER element of the XML we send to woods (the "envelope"):
 *
 *   <BirdsListRequest>
 *     <Body>
 *       <FacilityId>F-100</FacilityId>
 *       <FromAmount>100</FromAmount>
 *       <ToAmount>300</ToAmount>
 *     </Body>
 *   </BirdsListRequest>
 *
 * JAXB (Jakarta XML Binding) converts this object to that XML text and back.
 * The annotations say which element each field becomes:
 *   - @XmlRootElement: this class can be a whole XML document; its tag name
 *   - @XmlAccessorType(FIELD): JAXB reads/writes the fields directly (no getters)
 *   - @XmlElement(name = ...): the tag name for a field
 *
 * JAXB classes are kept "dumb": public fields, no logic. JAXB also needs a
 * no-argument constructor; Java adds one automatically when you write none.
 */
@XmlRootElement(name = "BirdsListRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class BirdsListRequest {

    @XmlElement(name = "Body", required = true)
    public BirdsListBody body;
}
