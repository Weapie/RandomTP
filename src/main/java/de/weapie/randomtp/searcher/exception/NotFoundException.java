package de.weapie.randomtp.searcher.exception;

public class NotFoundException extends IllegalArgumentException {

    private final String what;

    public NotFoundException(String what) {
        super(what + " was not found!");
        this.what = what;
    }

    public String getWhat() {
        return what;
    }

}