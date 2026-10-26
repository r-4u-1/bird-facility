package birds.woods.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The INNER <Body> of the request: the actual search fields.
 *
 * Everything is a String because that's what woods expects on the wire,
 * including "" (empty) for "no filter". Our own code uses BigDecimal and
 * Optional; BirdsListParameters converts between the two.
 *
 * No @XmlRootElement: <Body> never stands alone, it only appears inside
 * <BirdsListRequest>.
 *
 * propOrder fixes the order of the elements in the XML. Many older systems
 * reject the request if the order is different.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"facilityId", "fromAmount", "toAmount"})
public class BirdsListBody {

    @XmlElement(name = "FacilityId", required = true)
    public String facilityId;

    @XmlElement(name = "FromAmount", required = true)
    public String fromAmount;

    @XmlElement(name = "ToAmount", required = true)
    public String toAmount;
}
