package bn.blaszczyk.roseservice;

import java.util.Arrays;

import org.apache.logging.log4j.*;

import bn.blaszczyk.rosecommon.RoseException;
import bn.blaszczyk.rosecommon.controller.*;
import bn.blaszczyk.rosecommon.tools.CommonPreference;
import bn.blaszczyk.rosecommon.tools.LoggerConfigurator;
import bn.blaszczyk.rosecommon.tools.Preference;
import bn.blaszczyk.rosecommon.tools.Preferences;
import bn.blaszczyk.rosecommon.tools.TypeManager;
import bn.blaszczyk.roseservice.calculator.CalculatorEndpoint;
import bn.blaszczyk.roseservice.server.*;
import bn.blaszczyk.roseservice.tools.ServicePreference;
import bn.blaszczyk.roseservice.web.WebEndpoint;
import bn.blaszczyk.rose.model.Readable;

import static bn.blaszczyk.rosecommon.tools.Preferences.*;
import static bn.blaszczyk.rosecommon.tools.CommonPreference.*;
import static bn.blaszczyk.roseservice.tools.ServicePreference.*;

public class Launcher {
	
	private static final Logger LOGGER = LogManager.getLogger(Launcher.class);
	
	private static final Preference[][] PREFERENCES = new Preference[][]{ServicePreference.values(),CommonPreference.values()};

	private PersistenceController hibernateController;
	private CacheController cacheController;
	private ModelController controller;
	
	private RoseHandler handler;
	private RoseServer server;
	
	private Integer port;
	
	public void launch() throws RoseException
	{
		hibernateController = new PersistenceController();
		cacheController = new CacheController(hibernateController);
		controller = new ConsistencyDecorator(cacheController);
		
		handler = new RoseHandler();
		
		port = getIntegerValue(SERVICE_PORT);
		server = new RoseServer(port, handler);
		
		registerEndpoints();
		
		new Thread(this::preloadEntities,"Thread-Load-Entities").start();
		
		try
		{
			server.startServer();
		}
		catch(RoseException e)
		{
			LOGGER.error("Error starting rose service", e);
		}
		
		
	}

	protected void registerEndpoints()
	{
		handler.registerEndpointOptional("entity", new EntityEndpoint(controller),ENTITY_ENDPOINT_ACTIVE);
		handler.registerEndpointOptional("server", new ServerEndpoint(this),SERVICE_ENDPOINT_ACTIVE);
		handler.registerEndpointOptional("web", new WebEndpoint("http://localhost:" + port),WEB_ENDPOINT_ACTIVE);
		handler.registerEndpointOptional("calc", new CalculatorEndpoint(),CALC_ENDPOINT_ACTIVE);
		handler.registerEndpointOptional("file", new FileEndpoint(),FILE_ENDPOINT_ACTIVE);
	}
	
	public void stop()
	{
		try
		{
			controller.close();
			server.stopServer();
		}
		catch (RoseException e)
		{
			LOGGER.error("Error stopping rose service", e);
		}
		server = null;
		handler = null;
		controller = null;
		cacheController = null;
		hibernateController = null;
	}

	public RoseServer getServer()
	{
		return server;
	}

	public ModelController getController()
	{
		return controller;
	}
	
	public Preference[][] getPreferences()
	{
		return PREFERENCES;
	}
	
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("No Rose model file specified.");
			System.exit(1);
		}
		final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		Preferences.setMainClass(RoseServer.class);
		Preferences.cacheArguments(subArgs, PREFERENCES);
		TypeManager.parseRoseFile(Launcher.class.getClassLoader().getResourceAsStream(args[0]));
		LoggerConfigurator.configureLogger(CommonPreference.BASE_DIRECTORY, CommonPreference.LOG_LEVEL);
		try 
		{
			new Launcher().launch();
		}
		catch (RoseException e) 
		{
			LogManager.getLogger(Launcher.class).error("Error launching Service",e);
		}
	}

	private void preloadEntities()
	{
		try
		{
			for(Class<? extends Readable> type : TypeManager.getEntityClasses())
				cacheController.getEntities(type);
		}
		catch (RoseException e) 
		{
			LOGGER.error("error preloading entities", e);
		}
	}
	
}
