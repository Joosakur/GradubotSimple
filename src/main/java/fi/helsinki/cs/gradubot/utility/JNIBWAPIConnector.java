package fi.helsinki.cs.gradubot.utility;

import fi.helsinki.cs.gradubot.production.optimize.meta.ACOMetaOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.meta.ACOOneParamOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.solutions.OptimizerRunner;
import jnibwapi.BWAPIEventListener;
import jnibwapi.Position;

public class JNIBWAPIConnector implements BWAPIEventListener{

    private OptimizerRunner optimizerRunner;
    private ACOMetaOptimizer metaOptimizer;
    private ACOOneParamOptimizer oneParamOptimizer;

    @Override
    public void connected() {
        System.out.println("Connected");
        optimizerRunner = new OptimizerRunner();
        //metaOptimizer = new ACOMetaOptimizer();
        //oneParamOptimizer = new ACOOneParamOptimizer();
    }

    @Override
    public void matchStart() {
        System.out.println("Match starting");
    }

    @Override
    public void matchFrame() {
        optimizerRunner.run();
        //metaOptimizer.run();
        //oneParamOptimizer.run();
    }

    @Override
    public void matchEnd(boolean winner) {
        System.out.println("Match ended");
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void sendText(String text) {

    }

    @Override
    public void receiveText(String text) {

    }

    @Override
    public void playerLeft(int playerID) {

    }

    @Override
    public void nukeDetect(Position p) {

    }

    @Override
    public void nukeDetect() {

    }

    @Override
    public void unitDiscover(int unitID) {

    }

    @Override
    public void unitEvade(int unitID) {

    }

    @Override
    public void unitShow(int unitID) {

    }

    @Override
    public void unitHide(int unitID) {

    }

    @Override
    public void unitCreate(int unitID) {

    }

    @Override
    public void unitDestroy(int unitID) {

    }

    @Override
    public void unitMorph(int unitID) {

    }

    @Override
    public void unitRenegade(int unitID) {

    }

    @Override
    public void saveGame(String gameName) {

    }

    @Override
    public void unitComplete(int unitID) {

    }

    @Override
    public void playerDropped(int playerID) {

    }
}
