package de.weapie.randomtp.searcher.validators;

import de.weapie.randomtp.searcher.RandomLocationSearcher;
import org.bukkit.Location;

public abstract class LocationValidator {

    private String type;

    public LocationValidator(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract boolean validate(RandomLocationSearcher searcher, Location location);

}
