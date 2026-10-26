package birds.woods.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Woods' version of feathers:
 *
 *   <Feathers>
 *     <Type>Contour</Type>
 *     <LengthMm>65</LengthMm>
 *     <PrimaryColor>Brown</PrimaryColor>
 *     <SecondaryColor>Orange</SecondaryColor>
 *   </Feathers>
 *
 * Compare with our model: Feathers(type, lengthMm, Colors(primary, secondary)).
 * Same information, different shape. That difference is exactly why the woods
 * classes and our models are kept apart.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WoodsBirdsFeathers {

    @XmlElement(name = "Type")
    public String type;

    @XmlElement(name = "LengthMm")
    public String lengthMm;

    @XmlElement(name = "PrimaryColor")
    public String primaryColor;

    @XmlElement(name = "SecondaryColor")
    public String secondaryColor;
}
