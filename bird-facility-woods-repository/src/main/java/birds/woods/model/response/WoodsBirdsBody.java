package birds.woods.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

/**
 * The <Body> of woods' answer: a wrapped list of birds.
 *
 *   <Body>
 *     <Birds>            <- @XmlElementWrapper: the element AROUND the list
 *       <Bird>...</Bird> <- @XmlElement: one element per list item
 *       <Bird>...</Bird>
 *     </Birds>
 *   </Body>
 *
 * If woods sends an empty <Birds/> or leaves it out, "birds" can be null.
 * BirdsRepositoryImpl handles that, at the boundary to the external system.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WoodsBirdsBody {

    @XmlElementWrapper(name = "Birds")
    @XmlElement(name = "Bird")
    public List<WoodsBird> birds;
}
