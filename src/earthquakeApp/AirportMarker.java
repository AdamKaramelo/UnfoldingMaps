package earthquakeApp;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 *
 */
public class AirportMarker extends CommonMarker {
	public static List<SimpleLinesMarker> routes;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		if(this.getProperty("Continent").equals("Pacific")) {
			pg.fill(173, 216, 230);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Atlantic")) {
			pg.fill(0,0,255);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Indian")) {
			pg.fill(0,0,139);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Europe")) {
			pg.fill(255,0,0);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("America")) {
			pg.fill(255,69,0);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Asia")) {
			pg.fill(255,255,0);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Africa")) {
			pg.fill(153,50,204);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Australia")) {
			pg.fill(139,69,19);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Arctic")) {
			pg.fill(255,255,255);
			pg.ellipse(x, y, 5, 5);
		}else if (this.getProperty("Continent").equals("Antarctica")) {
			pg.fill(255,182,193);
			pg.ellipse(x, y, 5, 5);
		}else {
		pg.fill(11);
		pg.ellipse(x, y, 5, 5);
		}
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		 // show rectangle with title
		
		// show routes
		
		String title = (String)this.getProperty("name") + ", " + (String)this.getProperty("city") + ", " + (String)this.getProperty("country");
		pg.pushStyle();

		pg.rectMode(PConstants.CORNER);

		pg.stroke(110);
		pg.fill(255, 255, 255);
		pg.rect(x, y + 15, pg.textWidth(title) + 6, 18, 5);

		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3, y + 18);

		pg.popStyle();
	}
}
