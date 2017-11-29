package fr.badblock.bungee.loader;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import fr.badblock.bungee.BadBungee;
import fr.badblock.bungee.config.BadBungeeConfig;
import fr.badblock.bungee.config.ConfigLoader;
import fr.badblock.bungee.link.bungee.BungeeTask;
import fr.badblock.bungee.utils.PackageUtils;
import fr.toenga.common.tech.mongodb.MongoConnector;
import fr.toenga.common.tech.mongodb.MongoService;
import fr.toenga.common.tech.rabbitmq.RabbitConnector;
import fr.toenga.common.tech.rabbitmq.RabbitService;
import fr.toenga.common.utils.i18n.I18n;
import fr.toenga.common.utils.permissions.PermissionsManager;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class BungeeLoader
{

	private BadBungee		badBungee;
	private BadBungeeConfig	config;

	public BungeeLoader(BadBungee badBungee)
	{
		setBadBungee(badBungee);
		setInstance();
		setGson();
		loadConfig();
		loadI18n();
		loadRabbit();
		loadMongo();
		loadBungeeLinker();
		loadListeners();
		loadPermissions();
	}

	private void setInstance()
	{
		BadBungee.setInstance(getBadBungee());
	}

	private void setGson()
	{
		getBadBungee().setGson(new Gson());
		getBadBungee().setPrettyGson(new GsonBuilder().setPrettyPrinting().create());
	}

	private void loadConfig()
	{
		setConfig(new ConfigLoader().load());
		getBadBungee().setConfig(getConfig());
	}

	private void loadI18n()
	{
		File i18n = new File(getBadBungee().getDataFolder(), "i18n/");
		try
		{
			I18n.getInstance().load(i18n);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private void loadRabbit()
	{
		getBadBungee().setRabbitService(RabbitConnector.getInstance().registerService(new RabbitService("default", getConfig().getRabbitSettings())));
	}

	private void loadMongo()
	{
		getBadBungee().setMongoService(MongoConnector.getInstance().registerService(new MongoService("default", getConfig().getMongoSettings())));
	}

	private void loadBungeeLinker()
	{
		new BungeeTask();
	}

	private void loadListeners()
	{
		try
		{
			PackageUtils.instanciateListeners(getBadBungee(),
					"fr.xmalware.badblock.bungee.rabbit.listeners", // Rabbit listeners
					// Commands
					"fr.xmalware.badblock.bungee._plugins.commands.admin",
					"fr.xmalware.badblock.bungee._plugins.commands.modo",
					"fr.xmalware.badblock.bungee._plugins.commands.basic",
					// Listeners
					"fr.xmalware.badblock.bungee._plugins.listeners.logins.checkLogin",
					"fr.xmalware.badblock.bungee._plugins.listeners.logins.loadPlayer",
					"fr.xmalware.badblock.bungee._plugins.listeners.motd",
					"fr.xmalware.badblock.bungee._plugins.listeners.permissions",
					"fr.xmalware.badblock.bungee._plugins.listeners.punishments"
					);
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	private void loadPermissions()
	{
		MongoService mongoService = BadBungee.getInstance().getMongoService();
		DB db = mongoService.getDb();
		DBCollection collection = db.getCollection("permissions");
		BasicDBObject query = new BasicDBObject();
		DBCursor cursor = collection.find(query);
		if (cursor.hasNext())
		{
			DBObject dbObject = cursor.next();
			String json = getBadBungee().getGson().toJson(dbObject.get("groups"));
			PermissionsManager.createPermissionManager(getBadBungee().getGson().fromJson(json, JsonObject.class), "bungee");
		}
	}

}
