package fi.helsinki.cs.gradubot;

import fi.helsinki.cs.gradubot.utility.JNIBWAPIConnector;
import jnibwapi.JNIBWAPI;

/**
 * Created by joza on 12.10.2014.
 */
public class GraduBot {

    public static JNIBWAPI jnibwapi;

    public static void main(String... args) {
        jnibwapi = new JNIBWAPI(new JNIBWAPIConnector(), false);
        jnibwapi.start();
    }


}
