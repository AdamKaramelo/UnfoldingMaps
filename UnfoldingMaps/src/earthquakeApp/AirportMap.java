package earthquakeApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * An applet that shows airports (and routes) on a world map.
 * 
 *
 */
public class AirportMap extends PApplet {

	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;

	public void setup() {
		// setting up PAppler
		size(900, 700, OPENGL);

		// setting up map and default events
		map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.AerialProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");

		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();

		// create markers from features
		for (PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			m.setRadius(5);
			airportList.add(m);

			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());

		}

		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for (ShapeFeature route : routes) {

			// get source and destination airportIds
			int source = Integer.parseInt((String) route.getProperty("source"));
			int dest = Integer.parseInt((String) route.getProperty("destination"));

			// get locations for airports on route
			if (airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}

			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			for (Marker m : airportList) {
				if (m.getProperty("ID").equals(route.getProperty("destination"))) {
					sl.setColor(color(0, 0, 0));
					if (m.getProperty("Continent").equals("Pacific")) {
						sl.setColor(color(173, 216, 230));
					} else if (m.getProperty("Continent").equals("Atlantic")) {
						sl.setColor(color(0, 0, 255));
					} else if (m.getProperty("Continent").equals("Indian")) {
						sl.setColor(color(0, 0, 139));
					} else if (m.getProperty("Continent").equals("Europe")) {
						sl.setColor(color(255, 0, 0));
					} else if (m.getProperty("Continent").equals("America")) {
						sl.setColor(color(255, 69, 0));
					} else if (m.getProperty("Continent").equals("Asia")) {
						sl.setColor(color(255, 255, 0));
					} else if (m.getProperty("Continent").equals("Africa")) {
						sl.setColor(color(153, 50, 204));
					} else if (m.getProperty("Continent").equals("Australia")) {
						sl.setColor(color(139, 69, 19));
					} else if (m.getProperty("Continent").equals("Arctic")) {
						sl.setColor(color(255, 255, 255));
					} else if (m.getProperty("Continent").equals("Antarctica")) {
						sl.setColor(color(255, 182, 193));
					} else {
						sl.setColor(color(11));
					}
				}
			}
			// System.out.println(sl.getProperties());

			// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}

		for (Marker route : routeList) {
			route.setHidden(true);
		}

		// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);

		map.addMarkers(airportList);

	}

	public void draw() {
		background(0);
		map.draw();
		addKey();
	}

	private void addKey() {
		fill(255, 250, 240);

		int xbase = 15;
		int ybase = 50;

		rect(xbase, ybase, 170, 500);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Airports Map", 50, 75);

		fill(color(173, 216, 230));
		ellipse(xbase + 35, ybase + 50, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase + 35, ybase + 70, 12, 12);
		fill(color(0, 0, 139));
		ellipse(xbase + 35, ybase + 90, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase + 35, ybase + 110, 12, 12);
		fill(color(255, 69, 0));
		ellipse(xbase + 35, ybase + 130, 12, 12);
		fill(color(255, 255, 0));
		ellipse(xbase + 35, ybase + 150, 12, 12);
		fill(color(153, 50, 204));
		ellipse(xbase + 35, ybase + 170, 12, 12);
		fill(color(139, 69, 19));
		ellipse(xbase + 35, ybase + 190, 12, 12);
		fill(color(255, 255, 255));
		ellipse(xbase + 35, ybase + 210, 12, 12);
		fill(color(255, 182, 193));
		ellipse(xbase + 35, ybase + 230, 12, 12);

		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Pacific", xbase + 50, ybase + 50);
		text("Atlantic", xbase + 50, ybase + 70);
		text("Indian", xbase + 50, ybase + 90);
		text("Europe", xbase + 50, ybase + 110);
		text("America", xbase + 50, ybase + 130);
		text("Asia", xbase + 50, ybase + 150);
		text("Africa", xbase + 50, ybase + 170);
		text("Australia", xbase + 50, ybase + 190);
		text("Arctic", xbase + 50, ybase + 210);
		text("Antarctica", xbase + 50, ybase + 230);
		text("If you select an airport,", xbase + 7, ybase + 260);
		text("lines connecting to possible", xbase + 7, ybase + 275);
		text("flight destinations will", xbase + 7, ybase + 290);
		text("appear. The color of the ", xbase + 7, ybase + 305);
		text("line corresponds to the ", xbase + 7, ybase + 320);
		text("color of the markers, thus ", xbase + 7, ybase + 335);
		text("indicating the region ", xbase + 7, ybase + 350);
		text("you are flying to.", xbase + 7, ybase + 365);
	}

	/**
	 * Event handler that gets called automatically when the mouse moves.
	 */
	@Override
	public void mouseMoved() {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(airportList);
	}

	// If there is a marker selected
	private void selectMarkerIfHover(List<Marker> markers) {
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
			if (marker.isInside(map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

	/**
	 * The event handler for mouse clicks It will display an earthquake and its
	 * threat circle of cities Or if a city is clicked, it will display all the
	 * earthquakes where the city is in the threat circle
	 */
	@Override
	public void mouseClicked() {
		if (lastClicked != null) {
			unhideMarkers_and_Routes();
			lastClicked = null;
		} else if (lastClicked == null) {
			checkMarkers_and_RoutesForClick();
		}
	}

	private void unhideMarkers_and_Routes() {
		for (Marker marker : airportList) {
			marker.setHidden(false);
			((CommonMarker) marker).setLooped(false);
		}
		for (Marker route : routeList) {
			route.setHidden(true);
		}
	}

	private void checkMarkers_and_RoutesForClick() {
		if (lastClicked != null)
			return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : airportList) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) marker;
				for (Marker route : routeList) {
					/*
					 * System.out.println(route.getProperty("source"));
					 * System.out.println(lastClicked.getId());
					 */
					if (route.getProperty("source").equals(lastClicked.getProperty("ID"))) {
						route.setHidden(false);

						for (Marker munhide : airportList) {
							if (munhide != lastClicked
									&& route.getProperty("destination").equals(munhide.getProperty("ID"))) {
								munhide.setHidden(false);
								((CommonMarker) munhide).setLooped(true);
							}
						}
					}
				}
				// Hide all the other earthquakes and hide
				for (Marker mhide : airportList) {
					if (mhide != lastClicked && ((CommonMarker) mhide).getLooped() == false) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

}
