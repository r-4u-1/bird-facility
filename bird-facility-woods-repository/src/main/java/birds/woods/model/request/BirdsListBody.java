package birds.woods.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The INNER <Body> of the request: the actual search fields.
 *
 * Everything is a String because that's what woods expects on the wire,
 * including "" (empty) for "no filter". Dates are "yyyy-MM-dd". Our own code
 * uses BigDecimal, LocalDate and Optional; BirdsListParameters converts.
 *
 * No @XmlRootElement: <Body> never stands alone, it only appears inside
 * <BirdsListRequest>.
 *
 * propOrder fixes the order of the elements in the XML. Many older systems
 * reject the request if the order is different.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"facilityId", "fromAmount", "toAmount", "fromDate", "toDate"})
public class BirdsListBody {

    @XmlElement(name = "FacilityId", required = true)
    public String facilityId;

    @XmlElement(name = "FromAmount", required = true)
    public String fromAmount;

    @XmlElement(name = "ToAmount", required = true)
    public String toAmount;

    @XmlElement(name = "FromDate", required = true)
    public String fromDate;

    @XmlElement(name = "ToDate", required = true)
    public String toDate;
}
