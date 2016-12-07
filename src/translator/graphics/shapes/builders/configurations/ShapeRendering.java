package translator.graphics.shapes.builders.configurations;

/**
 *Property<br>
 * Hint to the implementation about what tradeoffs to make as it renders vector graphics elements such as 'path' elements and basic shapes such as circles and rectangles.<br>
 *<code>  auto | optimizeSpeed | crispEdges | geometricPrecision | inherit </code> (default).<br><br>
 *auto.<br>
 * Indicates that the user agent shall make appropriate tradeoffs to balance speed, crisp edges and geometric precision, but with geometric precision given more importance than speed and crisp edges.<br><br>
 * optimizeSpeed.<br>
 * Indicates that the user agent shall emphasize rendering speed over geometric precision and crisp edges. <br><br>
 * crispEdges.<br>
 * Indicates that the user agent shall attempt to emphasize the contrast between clean edges of artwork over rendering speed and geometric precision. To achieve crisp edges, the user agent might turn off anti-aliasing for all lines and curves or possibly just for straight lines which are close to vertical or horizontal. Also, the user agent might adjust line positions and line widths to align edges with device pixels.<br><br>
 * geometricPrecision.<br>
 * Indicates that the user agent shall emphasize geometric precision over speed and crisp edges.
 */
public enum ShapeRendering {
    auto, optimizeSpeed, crispEdges, geometricPrecision, inherit;
}
