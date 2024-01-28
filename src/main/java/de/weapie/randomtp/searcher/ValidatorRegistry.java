package de.weapie.randomtp.searcher;

import de.weapie.randomtp.searcher.validators.LocationValidator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidatorRegistry {

    private Map<String, LocationValidator> validators = new LinkedHashMap<>();

    /**
     * Get the map of currently set location validators
     * @return The map of validators
     */
    public Map<String, LocationValidator> getRaw() {
        return validators;
    }

    public Collection<LocationValidator> getAll() {
        return validators.values();
    }

    public LocationValidator add(LocationValidator validator) {
        return validators.put(validator.getType().toLowerCase(), validator);
    }

    public LocationValidator remove(LocationValidator validator) {
        return remove(validator.getType());
    }

    public LocationValidator remove(String type) {
        return validators.remove(type.toLowerCase());
    }

    public LocationValidator get(String type) {
        return validators.get(type.toLowerCase());
    }

}
