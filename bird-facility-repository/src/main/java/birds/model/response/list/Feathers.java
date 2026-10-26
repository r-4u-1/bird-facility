package birds.model.response.list;

/**
 * A bird's feathers. In the woods XML these are flat fields on <Feathers>;
 * here the colours are grouped into their own record.
 */
public record Feathers(String type, int lengthMm, Colors colors) {
}
