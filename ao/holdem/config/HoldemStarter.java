package ao.holdem.config;

import ao.ai.opp_model.OppModelTest;
import ao.persist.dao.PlayerHandleAccess;
import ao.util.rand.Rand;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 */
public class HoldemStarter
{
    public static void main(String args[]) throws Exception
    {
        Rand.nextBoolean();

        // configure log4j logging
        BasicConfigurator.configure();
        Logger.getLogger("org.hibernate").setLevel(Level.ERROR);

        Injector injector =
                Guice.createInjector(
                        PersistenceService
                                .usingHibernate()
                                .across(UnitOfWork.TRANSACTION)
                                .addAccessor(PlayerHandleAccess.class)
                                .buildModule(),
                        new HoldemConfig());
        injector.getInstance(PersistenceService.class).start();

//        injector.getInstance(DealerTest.class).realDealerTest();
//        injector.getInstance(DecisionTest.class).testDecisionTree();
//        injector.getInstance(
//                PredictPersistTest.class).testPredictionPersistqqance();
        injector.getInstance(OppModelTest.class).testOpponentModeling();
//        injector.getInstance(IrcRunner.class).run(
//                "C:\\alex\\data\\limit_holdem\\holdem\\199505");
//                "C:\\alex\\data\\irc_poker\\holdem\\200104");
//        injector.getInstance(IrcRunner.class).runOnSubdirs(
//                "C:\\alex\\data\\limit_holdem\\holdem");
//                "C:\\alex\\data\\irc_poker\\holdem3");
    }
}