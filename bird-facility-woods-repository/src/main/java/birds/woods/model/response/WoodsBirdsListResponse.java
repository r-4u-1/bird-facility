package birds.woods.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The OUTER element of the XML woods sends back:
 *
 *   <BirdsListResponse>
 *     <Body>
 *       <Birds>
 *         <Bird>...</Bird>
 *       </Birds>
 *     </Body>
 *   </BirdsListResponse>
 *
 * The "Woods" prefix on the class name marks it as woods' version of the data,
 * so it is never confused with our own BirdsListResponse model. The XML tag is
 * still "BirdsListResponse", because that's what woods sends.
 */
@XmlRootElement(name = "BirdsListResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class WoodsBirdsListResponse {

    @XmlElement(name = "Body")
    public WoodsBirdsBody body;
}
