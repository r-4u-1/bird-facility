package birds.woods.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * One <Bird> exactly as woods sends it:
 *
 *   <Bird>
 *     <Id>B-1</Id>
 *     <Name>Robin</Name>
 *     <Species>Erithacus rubecula</Species>
 *     <Price>250.00</Price>
 *     <FoodType>Insects</FoodType>
 *     <FoodGramsPerDay>12</FoodGramsPerDay>
 *     <Feathers>...</Feathers>
 *   </Bird>
 *
 * Everything is text, and the food fields are flat on the bird. Our own Bird
 * model uses real types and a nested Food record; BirdsRepositoryImpl converts.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WoodsBird {

    @XmlElement(name = "Id")
    public String id;

    @XmlElement(name = "Name")
    public String name;

    @XmlElement(name = "Species")
    public String species;

    @XmlElement(name = "Price")
    public String price;

    @XmlElement(name = "FoodType")
    public String foodType;

    @XmlElement(name = "FoodGramsPerDay")
    public String foodGramsPerDay;

    @XmlElement(name = "Feathers")
    public WoodsBirdsFeathers feathers;
}
