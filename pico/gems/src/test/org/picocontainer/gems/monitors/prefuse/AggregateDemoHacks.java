package org.picocontainer.gems.monitors.prefuse;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 * Demo application showcasing the use of AggregateItems to visualize groupings
 * of nodes with in a graph visualization.
 * 
 * This class uses the AggregateLayout class to compute bounding polygons for
 * each aggregate and the AggregateDragControl to enable drags of both nodes and
 * node aggregates.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
@SuppressWarnings("serial")
public class AggregateDemoHacks extends Display {

	private static final int LIGHT_BLUE = ColorLib.rgba(200, 200, 255, 150);

    private static final int LIGHT_GREEN = ColorLib.rgba(200, 255, 200, 150);

    private static final int LIGHT_RED = ColorLib.rgba(255, 200, 200, 150);

    private static final int WHITE = ColorLib.gray(255);

    private static final String GRAPH = "graph";

    private static final String NODES = "graph.nodes";

    private static final String EDGES = "graph.edges";

    private static final String EDGE_HEADS = "graph.edges.arrowheads";

    private static final String AGGR = "aggregates";

    private final int BLACK = ColorLib.gray(0);

    public AggregateDemoHacks(final Graph g) throws IOException {

        super(new Visualization());
        initDataGroups(g);

        Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        ((PolygonRenderer) polyR).setCurveSlack(0.15f);

        LabelRenderer labelRenderer = new LabelRenderer("type");
        labelRenderer.setRoundedCorner(8, 8);

        EdgeRenderer edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE);

        DefaultRendererFactory drf = new DefaultRendererFactory(labelRenderer);
        drf.add("ingroup('aggregates')", polyR);
        drf.add("ingroup('aggregates')", labelRenderer);
        drf.setDefaultEdgeRenderer(edgeRenderer);
        m_vis.setRendererFactory(drf);

        ActionList layout = buildActionList();
        m_vis.putAction("layout", layout);

        setSize(500, 500);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new AggregateDragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());

        m_vis.run("layout");
    }

    private ActionList buildColorActionList() {
        ColorAction nStroke = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStroke.setDefaultColor(ColorLib.gray(100));
        nStroke.add("_hover", ColorLib.gray(50));

        ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
        nFill.setDefaultColor(WHITE);
        nFill.add("_hover", ColorLib.gray(200));

        ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        nEdges.setDefaultColor(ColorLib.gray(100));
        ColorAction nEdgesHeads = new ColorAction(EDGES, VisualItem.FILLCOLOR);
        nEdgesHeads.setDefaultColor(ColorLib.gray(100));

        ColorAction classNames = new ColorAction(NODES, VisualItem.TEXTCOLOR);
        classNames.setDefaultColor(BLACK);

        ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
        aStroke.setDefaultColor(ColorLib.gray(200));
        aStroke.add("_hover", ColorLib.rgb(255, 100, 100));

        int[] palette = new int[] { LIGHT_RED, LIGHT_GREEN, LIGHT_BLUE };
        ColorAction aFill = new DataColorAction(AGGR, "type", Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(classNames);
        colors.add(nStroke);
        colors.add(nFill);
        colors.add(nEdges);
        colors.add(nEdgesHeads);
        colors.add(aStroke);
        colors.add(aFill);
        return colors;
    }

    private ActionList buildActionList() {
        ActionList colors = buildColorActionList();
        ActionList all = new ActionList(Activity.INFINITY);
        all.add(colors);
        all.add(new ForceDirectedLayout(GRAPH) {
            @Override
			protected float getSpringLength(final EdgeItem e) {
                return -0.3f;
            }

            @Override
			protected float getMassValue(final VisualItem n) {
                return 5.0f;
            }
        });

        all.add(new AggregateLayout(AGGR));
        all.add(new RepaintAction());
        return all;
    }

    private void initDataGroups(final Graph graph) {

        VisualGraph vg = m_vis.addGraph(GRAPH, graph);
        m_vis.setInteractive(EDGES, null, false);
        m_vis.setValue(NODES, null, VisualItem.SHAPE, Constants.SHAPE_ELLIPSE);

        AggregateTable at = m_vis.addAggregates(AGGR);
        at.addColumn(VisualItem.POLYGON, float[].class);
        at.addColumn("type", String.class);

        System.out.println("Aggregating...");
        Map aggregates = new HashMap();
        Iterator nodes = vg.nodes();
        while (nodes.hasNext()) {
            VisualItem node = (VisualItem) nodes.next();
            String pkg = node.getString("type");
            System.out.println(pkg);
            if (!aggregates.containsKey(pkg)) {
                AggregateItem aggregate = (AggregateItem) at.addItem();
                aggregate.setString("type", pkg);
                aggregates.put(pkg, aggregate);
            }
            AggregateItem aggregate = (AggregateItem) aggregates.get(pkg);
            aggregate.addItem(node);
        }
        System.out.println("done!");
    }

    public static JFrame demo(final Graph graph) throws IOException {
        AggregateDemoHacks ad = new AggregateDemoHacks(graph);
        JFrame frame = new JFrame("p r e f u s e  |  a g g r e g a t e d");
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

} // end of class AggregateDemo

/**
 * Layout algorithm that computes a convex hull surrounding aggregate items and
 * saves it in the "_polygon" field.
 */
final class AggregateLayout extends Layout {

    private final int m_margin = 5; // convex hull pixel margin

    private double[] m_pts; // buffer for computing convex hulls

    public AggregateLayout(final String aggrGroup) {
        super(aggrGroup);
    }

    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry,
     *      double)
     */
    @Override
	public void run(final double frac) {

        AggregateTable aggr = (AggregateTable) m_vis.getGroup(m_group);
        // do we have any to process?
        int num = aggr.getTupleCount();
        if (num == 0)
            return;

        // update buffers
        int maxsz = 0;
        for (Iterator aggrs = aggr.tuples(); aggrs.hasNext();)
            maxsz = Math.max(maxsz, 4 * 2 * ((AggregateItem) aggrs.next()).getAggregateSize());
        if (m_pts == null || maxsz > m_pts.length) {
            m_pts = new double[maxsz];
        }

        // compute and assign convex hull for each aggregate
        Iterator aggrs = m_vis.visibleItems(m_group);
        while (aggrs.hasNext()) {
            AggregateItem aitem = (AggregateItem) aggrs.next();

            int idx = 0;
            if (aitem.getAggregateSize() == 0)
                continue;
            VisualItem item;
            Iterator iter = aitem.items();
            while (iter.hasNext()) {
                item = (VisualItem) iter.next();
                if (item.isVisible()) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2 * 4;
                }
            }
            // if no aggregates are visible, do nothing
            if (idx == 0)
                continue;

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(m_pts, idx);

            // prepare viz attribute array
            float[] fhull = (float[]) aitem.get(VisualItem.POLYGON);
            if (fhull == null || fhull.length < nhull.length)
                fhull = new float[nhull.length];
            else if (fhull.length > nhull.length)
                fhull[nhull.length] = Float.NaN;

            // copy hull values
            for (int j = 0; j < nhull.length; j++)
                fhull[j] = (float) nhull[j];
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); // force invalidation
        }
    }

    private static void addPoint(final double[] pts, final int idx, final VisualItem item, final int growth) {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX()) - growth, minY = (b.getMinY()) - growth;
        double maxX = (b.getMaxX()) + growth, maxY = (b.getMaxY()) + growth;
        pts[idx] = minX;
        pts[idx + 1] = minY;
        pts[idx + 2] = minX;
        pts[idx + 3] = maxY;
        pts[idx + 4] = maxX;
        pts[idx + 5] = minY;
        pts[idx + 6] = maxX;
        pts[idx + 7] = maxY;
    }

} // end of class AggregateLayout

/**
 * Interactive drag control that is "aggregate-aware"
 */
final class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;

    protected final Point2D down = new Point2D.Double();

    protected final Point2D temp = new Point2D.Double();

    protected boolean dragged;

    /**
     * Creates a new drag control that issues repaint requests as an item is
     * dragged.
     */
    public AggregateDragControl() {
    }

    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    @Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if (!(item instanceof AggregateItem))
            setFixed(item, true);
    }

    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    @Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
        if (activeItem == item) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    @Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        dragged = false;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if (item instanceof AggregateItem)
            setFixed(item, true);
    }

    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    @Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        if (dragged) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }
    }

    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    @Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        dragged = true;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX() - down.getX();
        double dy = temp.getY() - down.getY();

        move(item, dx, dy);

        down.setLocation(temp);
    }

    protected static void setFixed(final VisualItem item, final boolean fixed) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                setFixed((VisualItem) items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }

    protected static void move(final VisualItem item, final double dx, final double dy) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                move((VisualItem) items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);
            item.setStartY(y);
            item.setX(x + dx);
            item.setY(y + dy);
            item.setEndX(x + dx);
            item.setEndY(y + dy);
        }
    }

} // end of class AggregateDragControl
