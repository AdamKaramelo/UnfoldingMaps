package earthquakeApp;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * {@link EarthquakeCityMap} - An application with an interactive map displaying
 * earthquake data from a live RSS feed: <a href= "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom">
 * http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom</a>
 * <p>
 * It utilizes UnfoldingMaps library <a href="http://unfoldingmaps.org/">http://unfoldingmaps.org/</a>
 * 
 */
public class EarthquakeCityMap extends PApplet {

	// Member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other
	// methods)

	// You can ignore this. It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {
		  
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
			earthquakesURL = "2.5_week.atom";
		} else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.AerialProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			// earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);

		//Reading in earthquake data and geometric properties
		// 1 - load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		// 2 - read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) {
			cityMarkers.add(new CityMarker(city));
		}

		// 3 - read in earthquake RSS feed
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		quakeMarkers = new ArrayList<Marker>();

		for (PointFeature feature : earthquakes) {
			// check if LandQuake
			if (isLand(feature)) {
				quakeMarkers.add(new LandQuakeMarker(feature));
			}
			// OceanQuakes
			else {
				quakeMarkers.add(new OceanQuakeMarker(feature));
			}
		}

		// could be used for debugging
		//printQuakes();
		
		// Add markers to map
		// NOTE: Country markers are not added to the map. They are used
		// for their geometric properties
		map.addMarkers(quakeMarkers);
		map.addMarkers(cityMarkers);
		
		sortAndPrint(100);
	} // End setup

	public void draw() {
		background(0);
		map.draw();
		addKey();

	}
	
	/**
	 * Sorts and prints the specified number of Earthquakes according to their magnitude. 
	 * @param numToPrint - amount of earthquakes to print
	 */
	private void sortAndPrint(int numToPrint) {
		EarthquakeMarker a[] = quakeMarkers.toArray(new EarthquakeMarker[quakeMarkers.size()]);
		Arrays.sort(a);
		if (numToPrint > a.length) {
			for (int i = 0; i < a.length; i++) {
				System.out.println(a[i].getTitle());
			}
		}else {
			for (int i = 0; i < numToPrint; i++) {
				System.out.println(a[i].getTitle());
			}
		}
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
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}

	/**
	 *  Selects the Marker the mouse is currently hovering
	 * @param markers - List of markers hovered
	 */
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
			unhideMarkers();
			lastClicked = null;
		} else if (lastClicked == null) {
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}
		}
	}

	/**
	 *  Helper method that will check if a city marker was clicked on. It will then hide all
	 *  other cities and show only the earthquakes inside the city's thread circle.
	 */
	private void checkCitiesForClick() {
		if (lastClicked != null)
			return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) marker;
				// Hide all the other earthquakes
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				//Hides only quakes further from thread circle
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker) mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation()) > quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}
	}

	/** Helper method that will check if an earthquake marker was clicked on.
	 * It will hide all the other markers and show the cities impacted by the 
	 * earthquake.
	 * 
	 */
	
	private void checkEarthquakesForClick() {
		if (lastClicked != null)
			return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker) m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				//Hide all city markers outside the thread circle
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) > marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

	/** 
	 * Method which loops over all markers and unhides them from map interface.
	 * 
	 */
	private void unhideMarkers() {
		for (Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}

		for (Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}

	/**
	 *  Helper method to draw key in GUI
	 */
	private void addKey() {
		fill(255, 250, 240);

		int xbase = 25;
		int ybase = 50;

		rect(xbase, ybase, 150, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase + 25, ybase + 25);

		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE,
				tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE, tri_ybase + CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);

		text("Land Quake", xbase + 50, ybase + 70);
		text("Ocean Quake", xbase + 50, ybase + 90);
		text("Size ~ Magnitude", xbase + 25, ybase + 110);

		fill(255, 255, 255);
		ellipse(xbase + 35, ybase + 70, 10, 10);
		rect(xbase + 35 - 5, ybase + 90 - 5, 10, 10);

		fill(color(255, 255, 0));
		ellipse(xbase + 35, ybase + 140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase + 35, ybase + 160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase + 35, ybase + 180, 12, 12);

		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase + 50, ybase + 140);
		text("Intermediate", xbase + 50, ybase + 160);
		text("Deep", xbase + 50, ybase + 180);

		text("Past hour", xbase + 50, ybase + 200);

		fill(255, 255, 255);
		int centerx = xbase + 35;
		int centery = ybase + 200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx - 8, centery - 8, centerx + 8, centery + 8);
		line(centerx - 8, centery + 8, centerx + 8, centery - 8);
	}

	/**
	 *  Checks whether this earthquake occurred on land. If it did, it sets the
	 *  "country" property of its PointFeature to the country where it occurred
	 *  and returns true. Otherwise it returns false.
	 *  
	 * @param earthquake
	 * @return
	 */
	private boolean isLand(PointFeature earthquake) {
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prints countries with number of earthquakes.
	 * Additionally prints ocean quakes.
	 */
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers) {
				EarthquakeMarker eqMarker = (EarthquakeMarker) marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}

	/**
	 *  Helper method to test whether a given earthquake is in a given country.
	 *  This will also add the country property to the properties of the earthquake
	 *  feature if it's in one of the countries.
	 *   
	 * @param earthquake 
	 * @param country
	 * @return
	 */
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if (country.getClass() == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker) country).getMarkers()) {

				// checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));

			return true;
		}
		return false;
	}

}
