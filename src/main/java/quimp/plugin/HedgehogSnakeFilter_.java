
/**
 * @file HedgehogSnakeFilter_.java
 * @date 14 Mar 2016
 */
package quimp.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.snakes.IQuimpBOAPoint2dFilter;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;

/**
 * Dummy test class emulates SNAKE plugin for QuimP
 * 
 * This filter moves \a i-th node by distance equal to distance between \a i-1 and \a i+1 node.
 * Effect is visible if Node Spacing in segmentation is around 2
 * 
 * To use this plugin one has to modify path with plugins in BOA call.
 * 
 * @author p.baniukiewicz
 *
 */
public class HedgehogSnakeFilter_ extends QWindowBuilder
        implements IQuimpBOAPoint2dFilter, IQuimpPluginSynchro, ChangeListener, ActionListener {

  static final Logger LOGGER = LoggerFactory.getLogger(HedgehogSnakeFilter_.class.getName());

  private List<Point2d> points;
  private ViewUpdater qcontext;
  private ParamList uiDefinition; /*!< Definition of UI for this plugin */
  private int every; // every point to delete

  /**
   * 
   */
  public HedgehogSnakeFilter_() {
    LOGGER.trace("Contructor of Plugin1 called");
    this.every = 3;
    uiDefinition = new ParamList(); // will hold ui definitions
    // configure window, names of UI elements are also names of variables
    // exported/imported by set/getPluginConfig
    uiDefinition.put("name", "testDelete"); // name of window
    uiDefinition.put("every", "spinner: 1: 100: 1:" + Integer.toString(every));
    buildWindow(uiDefinition); // construct ui (not shown yet)
  }

  @Override
  public int setup() {
    LOGGER.trace("setup of Plugin1 called");
    return DOES_SNAKES + CHANGE_SIZE;
  }

  @Override
  public void setPluginConfig(final ParamList par) throws QuimpPluginException {
    try {
      every = par.getIntValue("every");
      setValues(par); // populate loaded values to UI
    } catch (Exception e) {
      // we should never hit this exception as parameters are not touched
      // by caller they are only passed to configuration saver and
      // restored from it
      throw new QuimpPluginException("Wrong input argument-> " + e.getMessage(), e);
    }

  }

  @Override
  public ParamList getPluginConfig() {
    return getValues();
  }

  @Override
  public int showUi(boolean val) {
    LOGGER.trace("showUI of Plugin1 called with val " + val);
    toggleWindow(val);
    return 0;
  }

  @Override
  public String getVersion() {
    String trimmedClassName = getClass().getSimpleName();
    trimmedClassName = trimmedClassName.substring(0, trimmedClassName.length() - 1); // no _
    // _ at the end of class does not appears in final jar name, we need it to
    // distinguish between plugins
    return PropertyReader.readProperty(getClass(), trimmedClassName,
            "quimp/plugin/plugin.properties", "internalVersion");
  }

  /**
   * Simply modify every \c i-th node
   */
  @Override
  public List<Point2d> runPlugin() throws QuimpPluginException {
    every = getIntegerFromUI("every"); // transfer data from UI on plugin run
    LOGGER.debug("runPlugin of Plugin1 called with param every= " + every);
    ArrayList<Point2d> out = new ArrayList<>();
    Vector2d v;
    out.add(points.get(0));
    for (int i = 1; i < points.size() - 1; i++) {
      int sign = (i % 2) * 2 - 1; // -1 1 -1 1
      if (i % every == 0) {
        Vector2d cur = new Vector2d(points.get(i));
        Vector2d curm1 = new Vector2d(points.get(i - 1));
        Vector2d curp1 = new Vector2d(points.get(i + 1));
        v = new Vector2d(curp1.x - curm1.x, curp1.y - curm1.y);
        v = new Vector2d(-v.y, v.x); // parallel
        double l = v.length();
        v.normalize();
        v.scale(sign * l); // move current node to distance current-1 - current+1
        cur.add(v);
        out.add(new Point2d(cur));
      } else
        out.add(points.get(i));
    }
    out.add(points.get(points.size() - 1));
    return out;
  }

  @Override
  public void attachData(List<Point2d> data) {
    LOGGER.trace("attachData of Plugin1 called");
    points = data;
  }

  @Override
  public void attachContext(final ViewUpdater b) {
    qcontext = b;
  }

  @Override
  public void buildWindow(final ParamList def) {
    super.buildWindow(def); // window must be built first

    ((JSpinner) ui.get("every")).addChangeListener(this); // attach listener to selected ui
    applyB.addActionListener(this); // attach listener to apply button
  }

  @Override
  public void stateChanged(ChangeEvent ce) {
    LOGGER.trace("stateChanged of Plugin1 called");
    if (isWindowVisible() == true)
      qcontext.updateView();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    LOGGER.trace("actionPerformed of Plugin1 called");
    Object b = e.getSource();
    if (b == applyB) { // pressed apply, copy ui data to plugin
      qcontext.updateView(); // run plugin from QuimP context. Take care for transferring
                             // data from UI earlier (e.g. in runPlugin)
    }
  }

  @Override
  public String about() {
    return "This plugin is intended for testing purposes only\nAuthor: Piotr Baniukiewicz"
            + "\nmail: p.baniukiewicz@warwick.ac.uk";
  }

}
