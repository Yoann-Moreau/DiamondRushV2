package fr.ethilvan.diamondrushv2.region.pattern;

import fr.ethilvan.diamondrushv2.region.Region;

public abstract class Pattern {

	protected Region region;

	public Pattern(Region region) {
		this.region = region;
	}


	public abstract void create();
}
